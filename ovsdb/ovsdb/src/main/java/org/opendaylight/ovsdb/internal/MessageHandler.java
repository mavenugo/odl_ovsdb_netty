package org.opendaylight.ovsdb.internal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;


import org.opendaylight.ovsdb.table.Data;

import org.opendaylight.ovsdb.table.EchoReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

public class MessageHandler extends ChannelInboundHandlerAdapter {
    protected static final Logger logger = LoggerFactory.getLogger(MessageHandler.class);

    private Map<Long, MessageHandlerFuture> responseFutures = new HashMap<Long, MessageHandlerFuture>();

    public Future<Object> getResponse(long id) {
        MessageHandlerFuture responseFuture = new MessageHandlerFuture(Long.valueOf(id));
        responseFutures.put(Long.valueOf(id), responseFuture);
        return responseFuture;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        logger.info("ChannRead ==> " + msg.toString());
        JsonNode jsonNode;
        ObjectMapper mapper = new ObjectMapper();
        String strmsg = msg.toString();
        try {
            jsonNode = mapper.readTree(strmsg);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        if (jsonNode.has("method")) {
            String method = jsonNode.get("method").toString();
            if (method.contains("echo")) {
                EchoReply echoreply = new EchoReply();
                JsonNode echoReplyJnode = mapper.valueToTree(echoreply);
                // The Following String is what the Pojo Sends
                // String reply = "{\"result\":[], \"id\":\"echo\"}";

                logger.debug("Echo Reply DP ==>" + msg);
                ctx.writeAndFlush(echoReplyJnode.toString());
            }
        }
    }



/*
                logger.debug("==Echo Reply to the Echo Request from DP ==>>" + msg);
                ctx.writeAndFlush(echoReplyJnode.toString());
            }

        } else if (jsonNode.has("result")) {
            Long requestId = jsonNode.get("id").asLong();
            JsonParser parser = mapper.treeAsTokens(jsonNode.get("result"));
            try {
                Object response = mapper.readValue(parser, MessageMapper.getMapper().pop(requestId));
                MessageHandlerFuture future = responseFutures.get(requestId);
                if (future != null) {
                    future.gotResponse(requestId, response);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                JsonFactory factory = mapper.getJsonFactory();
                JsonParser jparse = null;
                //Map to Root Pojo Data.class
                jparse = factory.createParser(strmsg);
                JsonNode actualObj = mapper.readTree(jparse);
                Data hostreply = mapper.treeToValue(actualObj, Data.class);

                // Raw MSG String Return Print
                System.out.println(strmsg);
                //Example Return ID
                //   System.out.println("Json Response for channel ID ==> " + hostreply.Id());
                //Example Return Error
                System.out.println("Bridge Table UUID is ==> " + hostreply.getError());
                //Example Return Result
                System.out.println("Open_vSwitch Table UUID is ==> " + hostreply.getResults().getOvsTable().keySet());

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
*/


    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
}