package com.kamijou.apigateway;


import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.ValueFilter;
import com.kamijou.apicommon.model.entity.InterfaceInfo;
import com.kamijou.apicommon.model.entity.User;
import com.kamijou.apicommon.service.InnerInterfaceInfoService;
import com.kamijou.apicommon.service.InnerUserInterfaceInfoService;
import com.kamijou.apicommon.service.InnerUserService;
import com.kamijou.clientsdk.utils.SignUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 全局过滤
 */
@Slf4j
@Component
public class CustomGlobalFilter implements GlobalFilter, Ordered {

    private static final List<String> IP_WHITE_LIST = Arrays.asList("127.0.0.1");
    private static final String INTERFACE_HOST="http://localhost:8101";
    @DubboReference
    private InnerUserService innerUserService;
    @DubboReference
    private InnerUserInterfaceInfoService innerUserInterfaceInfoService;
    @DubboReference
    private InnerInterfaceInfoService innerInterfaceInfoService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        //1.请求日志
        ServerHttpRequest request = exchange.getRequest();
        String path = INTERFACE_HOST+request.getPath().value();
        String  method = request.getMethod().toString();
        log.info("请求标识" + request.getId());
        log.info("请求路径" + path);
        log.info("请求方法" + method);
        log.info("请求参数" + request.getQueryParams());
        String hostString = request.getLocalAddress().getHostString();
        log.info("请求来源地址" + hostString);
        ServerHttpResponse response = exchange.getResponse();

        //2.黑白名单
        if (!IP_WHITE_LIST.contains(hostString)) {
            return handleNoAuth(response);
        }

        //3.用户鉴权
        HttpHeaders headers = request.getHeaders();
        String accessKey = headers.getFirst("accessKey");
        String nonce = headers.getFirst("nonce");
        String timestamp = headers.getFirst("timestamp");
        String sign = headers.getFirst("sign");
        String body = headers.getFirst("body");
        //用户存在校验
        User invokeUser = null;

        try {
            invokeUser = innerUserService.getInvokeUser(accessKey);
        } catch (Exception e) {
            log.error("getInvokeUser error");
            throw new RuntimeException(e);
        }
        if (invokeUser == null) {
            return handleNoAuth(response);
        }
        if (Long.parseLong(nonce) > 10000) {
            return handleNoAuth(response);
        }

        //超时检查
        Long currentTime = System.currentTimeMillis() / 1000;
        final Long FIVE_MINUTES = 60 * 5L;
        if ((currentTime - Long.parseLong(timestamp)) >= FIVE_MINUTES) {
            return handleNoAuth(response);
        }

        //sK合法性校验
        String secretKey = invokeUser.getSecretKey();
        String serverSign = SignUtils.getSign(body, secretKey);
        if (sign == null || !sign.equals(serverSign)) {
            return handleNoAuth(response);
        }

        //请求接口存在性校验
        InterfaceInfo interfaceInfo = null;
        try {
            interfaceInfo= innerInterfaceInfoService.getInterfaceInfo(path, method);
        } catch (Exception e) {
            log.error("getInterfaceInfo error");
            throw new RuntimeException(e);
        }
        if (interfaceInfo==null){
            return handleNoAuth(response);
        }

//        //请求转发
//        Mono<Void> filter = chain.filter(exchange);

        //接口次数计算

        //调用失败，返回状态码
        if (response.getStatusCode() != HttpStatus.OK) {
            return handleInvokeError(response);
        }

        //调用 成功，返回响应日志
        Mono<Void> handleResponse = handleResponse(exchange, chain, interfaceInfo.getId(), invokeUser.getId());
        return handleResponse;

    }


    public Mono<Void> handleNoAuth(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.FORBIDDEN);
        return response.setComplete();
    }

    public Mono<Void> handleInvokeError(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        return response.setComplete();
    }

    private String modifyBody(String jsonStr) {
        com.alibaba.fastjson.JSONObject json = com.alibaba.fastjson.JSON.parseObject(jsonStr, Feature.AllowISO8601DateFormat);
        com.alibaba.fastjson.JSONObject.DEFFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm";
        return com.alibaba.fastjson.JSONObject.toJSONString(json, (ValueFilter) (object, name, value) -> value == null ? "" : value, SerializerFeature.WriteDateUseDateFormat);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    /**
     * 响应处理
     *
     * @param exchange
     * @param chain
     * @return
     */
    public Mono<Void> handleResponse(ServerWebExchange exchange, GatewayFilterChain chain,long interfaceInfoId,long userId) {
        try {
            ServerHttpResponse originalResponse = exchange.getResponse();
            //缓冲区工厂
            DataBufferFactory bufferFactory = originalResponse.bufferFactory();
            //获取响应码
            HttpStatus statusCode = originalResponse.getStatusCode();

            if (statusCode != HttpStatus.OK) {
                return chain.filter(exchange);//降级处理返回数据
            }
            ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
                //调用完转发接口后才会执行
                @Override
                public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                    if (body instanceof Flux) {
                        Flux<? extends DataBuffer> fluxBody = Flux.from(body);

                        return super.writeWith(
                                fluxBody.map(dataBuffer -> {
                                    // 7. 调用成功，接口调用次数 + 1 invokeCount
                                    try {
                                        innerUserInterfaceInfoService.invokeCount(interfaceInfoId, userId);
                                    } catch (Exception e) {
                                        log.error("invokeCount error", e);
                                    }
                                    byte[] content = new byte[dataBuffer.readableByteCount()];
                                    dataBuffer.read(content);
                                    DataBufferUtils.release(dataBuffer);//释放掉内存
                                    // 构建日志
                                    StringBuilder sb2 = new StringBuilder(200);
                                    List<Object> rspArgs = new ArrayList<>();
                                    rspArgs.add(originalResponse.getStatusCode());
                                    String data = new String(content, StandardCharsets.UTF_8); //data
                                    sb2.append(data);
                                    // 打印日志
                                    log.info("响应结果：" + data);
                                    return bufferFactory.wrap(content);
                                }));

                    } else {
                        log.error("<-- {} 响应code异常", getStatusCode());
                    }
                    return super.writeWith(body);
                }
            };
            return chain.filter(exchange.mutate().response(decoratedResponse).build());

        } catch (Exception e) {
            log.error("gateway log exception.\n" + e);
            return chain.filter(exchange);
        }
    }

}