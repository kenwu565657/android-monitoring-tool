package com.android.monitoring;

import com.android.monitoring.handler.WebSocketMessageHandler;
import com.android.monitoring.handler.WebSocketMessageHandlerFactory;
import com.android.monitoring.helper.ObjectMapperInstance;
import com.android.monitoring.pojo.WebSocketMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ChannelHandler.Sharable
public class MyWebsocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private static final Logger logger = LoggerFactory.getLogger(MyWebsocketHandler.class);

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        logger.info("new client connected: {}", ctx.channel().id().asLongText());
        WebsocketConnectionMap.getWebSocketClientChannelMap().put(ctx.channel().id().asLongText(), ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) {
        String text = msg.text();
        try {
            // decode string to WebSocketMessage object
            ObjectMapper objectMapper = ObjectMapperInstance.INSTANCE.getObjectMapper();
            WebSocketMessage websocketMessage = objectMapper.readValue(text, WebSocketMessage.class);
            websocketMessage.setSourceConnectionId(ctx.channel().id().asLongText());
            logger.info("received: header={}, body={}, sourceConnectionId={}", websocketMessage.getHeader(), websocketMessage.getBody(), websocketMessage.getSourceConnectionId());

            // handle different types of messages
            WebSocketMessageHandler webSocketMessageHandler = WebSocketMessageHandlerFactory.INSTANCE.getWebSocketMessageHandler(websocketMessage);
            var webSocketResponseMessage = webSocketMessageHandler.handleAndResponse(ctx, websocketMessage);
            if (null == webSocketResponseMessage) {
                return;
            }
            logger.info("response: header={}, body={}", webSocketResponseMessage.getHeader(), webSocketResponseMessage.getBody());

            // send response back to client
            var webSocketResponseMessageJson = ObjectMapperInstance.INSTANCE.getObjectMapper().writeValueAsString(webSocketResponseMessage);
            if (StringUtils.isNotBlank(webSocketResponseMessageJson)) {
                ctx.channel().write(new TextWebSocketFrame(webSocketResponseMessageJson));
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("deserialize fail: {}", text, e);
            // ctx.channel().writeAndFlush(new TextWebSocketFrame("{}"));
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext channelHandlerContext) {
        channelHandlerContext.flush();
        channelHandlerContext.fireChannelReadComplete();
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        logger.info("# client registered...：   {} ...", ctx.channel());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        logger.info("# client disconnected {} ", ctx.channel());
        WebsocketConnectionMap.removeChannel(ctx.channel().id().asLongText());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        logger.info("# client out... : {}", ctx.channel());
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent e) {

            if (e.state() == IdleState.ALL_IDLE) {
            }
        }

        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        logger.error("Connection error {}, cause: {}", ctx.channel().id().asLongText(), cause.getMessage());
        // WebsocketConnectionMap.getChannelGroup().remove(ctx.channel());
        // ctx.close();
    }
}
