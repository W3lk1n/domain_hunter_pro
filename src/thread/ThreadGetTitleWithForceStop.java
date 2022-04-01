package thread;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import burp.BurpExtender;
import burp.IBurpExtenderCallbacks;
import burp.IExtensionHelpers;

public class ThreadGetTitleWithForceStop extends Thread{
	private Set<String> domains;
	private List<Producer> plist;

	private static IBurpExtenderCallbacks callbacks = BurpExtender.getCallbacks();//静态变量，burp插件的逻辑中，是可以保证它被初始化的。;
	public PrintWriter stdout = new PrintWriter(callbacks.getStdout(), true);
	public PrintWriter stderr = new PrintWriter(callbacks.getStderr(), true);
	public IExtensionHelpers helpers = callbacks.getHelpers();
	private int threadNumber;

	public ThreadGetTitleWithForceStop(Set<String> domains,int threadNumber) {
		this.domains = domains;
		this.threadNumber = threadNumber;
	}

	@Override
	public void run(){
		stdout.println(String.format("~~~~~~~~~~~~~use %s threads~~~~~~~~~~~~~",threadNumber));
		stdout.println("~~~~~~~~~~~~~Start threading Get Title~~~~~~~~~~~~~ total task number: "+domains.size());
		BlockingQueue<String> domainQueue = new LinkedBlockingQueue<String>();//use to store domains
		domainQueue.addAll(domains);

		plist = new ArrayList<Producer>();

		for (int i=0;i<=threadNumber;i++) {
			Producer p = new Producer(domainQueue,i);
			//Producer p = new Producer(callbacks,domainQueue,sharedQueue,i);
			p.setDaemon(true);//将子线程设置为守护线程，会随着主线程的结束而立即结束。只有当JVM也退出时才可以！
			p.start();
			plist.add(p);
		}


		try {
			for (Producer p:plist) {
				p.join();
			}
			//让主线程等待各个子线程执行完成，才会结束。
			//https://www.cnblogs.com/zheaven/p/12054044.html
		} catch (InterruptedException e) {
			stdout.println(getName()+": force stop received");
			for (Producer p:plist) {
				p.interrupt();//没有起作用，奇怪！为什么呢？
				p.setStopflag(true);//由于当前主线程退出，并非是JVM退出，子线程还会继续运行，必须主动中断子线程才可以。
			}
			e.printStackTrace();
		}

		stdout.println(getName()+" finished");
	}

	@Deprecated
	public void stopThreads() {
		for (Producer p:plist) {
			p.setStopflag(true);
		}
		stdout.println("~~~~~~~~~~~~~send stop message to all sub-threads, wait them to exit!~~~~~~~~~~~~~");
	}

	public void forceStopThreads() {
		this.interrupt();//将子线程都设置为守护线程，会随着主线程的结束而立即结束！错了！只有当主线程退出时，JVM也退出才会这样！
		stdout.println("~~~~~~~~~~~~~force stop main thread,all sub-threads will exit!~~~~~~~~~~~~~");
	}
}
