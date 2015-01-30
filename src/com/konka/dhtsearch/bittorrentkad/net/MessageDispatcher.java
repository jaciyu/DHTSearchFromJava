package com.konka.dhtsearch.bittorrentkad.net;

import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import com.konka.dhtsearch.Node;
import com.konka.dhtsearch.bittorrentkad.concurrent.CompletionHandler;
import com.konka.dhtsearch.bittorrentkad.concurrent.FutureCallback;
import com.konka.dhtsearch.bittorrentkad.krpc.KadMessage;
import com.konka.dhtsearch.bittorrentkad.krpc.KadRequest;
import com.konka.dhtsearch.bittorrentkad.net.filter.MessageFilter;

/**
 * Handle all the messages different states. A request state: init -> sent -> response received -> callback invoked
 * 
 * @处理所有信息的不同状态 A message state: init -> expecting -> message received -> callback invoked -> back to expecting or end
 * 
 *
 * @param
 */
public class MessageDispatcher {

	// state
	private final String transactionID;// 返回消息的标识，t
	private KadRequest kadRequest;

 

 

	public String getTransactionID() {
		return transactionID;
	}

	private CompletionHandler<KadMessage, String> callback;
	private boolean isConsumbale = true;// 一个开关，在没有收到信息前可以取消
	private long timeout = 5 * 60 * 1000;// 15分钟超时
	private final Set<MessageFilter> filters = new HashSet<MessageFilter>();
	private TimerTask timeoutTimerTask = null;
	private final AtomicBoolean isDone;
	private final Timer timer;
	private final KadServer mKadServer;
	private final static Set<MessageDispatcher> messageDispatchers = new HashSet<MessageDispatcher>();

	public static Set<MessageDispatcher>  getMessageDispatchers() {
		return messageDispatchers;
	}

	private void expect() {
		messageDispatchers.add(this);
	}

	/**
	 * 根据tag transaction 查找对于的请求
	 * 
	 * @param tag
	 * @return
	 */
	public static MessageDispatcher findMessageDispatcherByTag(String tag) {
		for (MessageDispatcher messageDispatcher : messageDispatchers) {
//			System.out.println("messageDispatcher.getTag()="+messageDispatcher.getTransactionID());
//			System.out.println("tag="+tag);
			if (messageDispatcher.getTransactionID() != null && messageDispatcher.getTransactionID().equals(tag)) {
				return messageDispatcher;
			}
		}
		return null;
	}

	private void cancelExpect() {
		messageDispatchers.remove(this);
	}

	public MessageDispatcher(Timer timer, KadServer kadServer,String transaction) {
		expect();
		this.timer = timer;
		this.mKadServer = kadServer;
		this.isDone = new AtomicBoolean(false);
		this.transactionID = transaction;
	}

	public void cancel(Throwable exc) {
		if (!isDone.compareAndSet(false, true))
			return;

		if (timeoutTimerTask != null)
			timeoutTimerTask.cancel();
		cancelExpect();
		if (callback != null)
			callback.failed(exc, transactionID);
	}

	// returns true if should be handled
	boolean shouldHandleMessage(KadMessage m) {
//		System.out.println("循环");
		for (MessageFilter filter : filters) {
			if (!filter.shouldHandle(m)){
				return false;
			}
		}
		return true;
	}

	public void handle(KadMessage msg) {
		if(!shouldHandleMessage(msg)){ // 过滤
			return;
		}
		 

		if (isDone.get())// 是否done了
			return;

		if (timeoutTimerTask != null)// timeoutTimerTask如果被实例化了，要取消掉
			timeoutTimerTask.cancel();

		if (isConsumbale) {
			if (!isDone.compareAndSet(false, true))// 如果是正在运行的，
				return;
		}

		if (callback != null)
			callback.completed(msg, transactionID);
	}

	public MessageDispatcher addFilter(MessageFilter filter) {
		filters.add(filter);
		return this;
	}

	public MessageDispatcher setCallback(String attachment, CompletionHandler<KadMessage, String> callback) {
		this.callback = callback;
//		this.transactionID = attachment;
		return this;
	}

	public MessageDispatcher setTimeout(long t, TimeUnit unit) {
		timeout = unit.toMillis(t);
		return this;
	}

	public MessageDispatcher setConsumable(boolean consume) {
		isConsumbale = consume;
		return this;
	}

	public MessageDispatcher register() {
		// expecters.add(this);
		setupTimeout();
		return this;
	}

	public Future<KadMessage> futureRegister() {

		FutureCallback<KadMessage, String> f = new FutureCallback<KadMessage, String>() {
			@Override
			public synchronized boolean cancel(boolean mayInterruptIfRunning) {
				MessageDispatcher.this.cancel(new CancellationException());
				return super.cancel(mayInterruptIfRunning);
			};
		};

		setCallback(null, f);
		setupTimeout();

		return f;
	}

	private void setupTimeout() {
		if (!isConsumbale)
			return;
		timeoutTimerTask = new TimerTask() {
			@Override
			public void run() {
				MessageDispatcher.this.cancel(new TimeoutException());
			}
		};
		timer.schedule(timeoutTimerTask, timeout);
	}

	public KadRequest getKadRequest() {
		return kadRequest;
	}

	/**
	 * 如果要收到返回的消息，请使用此处的send（也可以直接使用方法体）
	 * 
	 * @param to
	 * @param req
	 */
	public void send(Node to, KadRequest req) {
		setConsumable(true);
		try {
			/*
			 * if (!outstandingRequests.offer(this, timeout, TimeUnit.MILLISECONDS)) throw new RejectedExecutionException();
			 */
			// outstandingRequests.put(this);
			expect();
			mKadServer.send(to, req);
			kadRequest = req;
			setupTimeout();
		} catch (Exception e) {
			cancel(e);
		}
	}

	public Future<KadMessage> futureSend(Node to, KadRequest req) {
		FutureCallback<KadMessage, String> f = new FutureCallback<KadMessage, String>();
		setCallback(null, f);
		expect();
		send(to, req);
		return f;
	}
}
