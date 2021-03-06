package io.mycat.mycat2;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import io.mycat.mycat2.cmd.ClientAuthProcessor;
import io.mycat.mycat2.cmd.DirectPassSQLProcessor;
import io.mycat.proxy.BufferPool;
import io.mycat.proxy.DefaultDirectProxyHandler;

/**
 * 代理MySQL的ProxyHandler
 * 
 * @author wuzhihui
 *
 */
public class MySQLProxyHandler extends DefaultDirectProxyHandler<MySQLSession> {

	

	public void onFrontConnected(BufferPool bufPool, Selector nioSelector, SocketChannel frontChannel)
			throws IOException {
		logger.info("MySQL client connected  ." + frontChannel);

		MySQLSession session = new MySQLSession(bufPool, nioSelector, frontChannel);
		session.bufPool = bufPool;
		session.nioSelector = nioSelector;
		session.frontChannel = frontChannel;
		InetSocketAddress clientAddr = (InetSocketAddress) frontChannel.getRemoteAddress();
		session.frontAddr = clientAddr.getHostString() + ":" + clientAddr.getPort();
		SelectionKey socketKey = frontChannel.register(nioSelector, SelectionKey.OP_READ, session);
		session.frontKey = socketKey;
		
		session.setCurrentSQLProcessor(ClientAuthProcessor.INSTANCE);
		session.sendAuthPackge();
		


	}

	@Override
	public void onFrontReaded(MySQLSession userSession) throws IOException {
		
		userSession.getCurSQLProcessor().handFrontPackage(userSession);
		
	}

	@Override
	public void onBackendReaded(MySQLSession userSession) throws IOException {

		userSession.getCurSQLProcessor().handBackendPackage(userSession);

	}

}
