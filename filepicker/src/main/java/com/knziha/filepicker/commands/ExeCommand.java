package com.knziha.filepicker.commands;

import android.os.Build;
import android.util.Log;

import com.knziha.filepicker.utils.CMNF;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.locks.ReadWriteLock;

import static java.lang.Runtime.getRuntime;

/**
 * Created by Kappa 2016
 */
public class ExeCommand {
	private Thread CountorThread, WorkerThread;
    //Shell进程 和 对应进程的3个流
    private Process process;
    private BufferedReader errorResult;
    private DataOutputStream writer;
    /** 是否同步 */
    private boolean bSynchronous;
    //表示shell进程是否还在运行
    private boolean bRunning = false;
    private boolean stopped = false;
    private boolean root = false;

	/** 同步锁 */
	ReadWriteLock lock;
    private StringBuffer result;
	private boolean bDisposable;

	public ExeCommand disPosable() {
		bDisposable=true;
		return this;
	}

	public ExeCommand listener(OnOutputListener _cmdl) {
		cmdl = _cmdl;
		return this;
	}

	public ExeCommand root() {
		root = true;
		return this;
	}

	public interface OnOutputListener {
    	void OnOutput(String line);
    }

	public interface finallyDo{
		void doit(ExeCommand cmdThis);
	}

	private OnOutputListener cmdl;
    public finallyDo finallyDo;

    public ExeCommand() {
        this(true);
    }

    public ExeCommand(ExeCommand cmdOld) {
		this(false);
		//CMNF.Log("PlayService","reConstruct @@pre0完成!!!");
    	finallyDo = cmdOld.finallyDo;
    	cmdOld.stop();
		//CMNF.Log("PlayService","reConstruct @@pre1完成!!!");
    	cmdl = cmdOld.cmdl;
		CMNF.Log("PlayService","reConstruct @@pre2完成!!!");
	}

	public ExeCommand(boolean synchronous) {
		bSynchronous = synchronous;
		init();
	}

	public void doit() {
		finallyDo.doit(this);
	}

    public boolean isRunning() {
        return bRunning;
    }

    /** Get cmd shell */
    private void init(){
        try {
            process = getRuntime().exec(root?"su":"sh");
			errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			writer = new DataOutputStream(process.getOutputStream());
        } catch (Exception e) { e.printStackTrace(); }
    }

	/** Run one line of command */
    public int run(String...commands) {
    	if(stopped)  init();
		CMNF.Log("PlayService", "!!!running command:",commands);
		stopped = false;
    	bRunning = true;
        if (commands == null || commands.length == 0) {
            return 0;
        }

        if(writer ==null) return -1;

        try {
			for(String cI:commands)
            	writer.write(cI.getBytes());
            writer.flush();

            CountorThread = new Thread(() -> {
				String line;
				//Lock writeLock = lock.writeLock();
				try {
					while ((line=errorResult.readLine()) != null) {
						if(stopped) break;

							CMNF.Log("fatal PlayService","CountorThread : "+line);
							line += "\n";
							if(!stopped && cmdl!=null) cmdl.OnOutput(line);
							//writeLock.lock(); result.append(line); writeLock.unlock();
					}
				} catch (Exception e) {
					CMNF.Log("PlayService", "read ErrorStream exception:" + e.toString());
					line=null;
					e.printStackTrace();
				}
				CMNF.Log("read ErrorStream 结束 ");
			});
            CountorThread.start();

            WorkerThread = new Thread(() -> {
				try {
					process.waitFor();
					CountorThread.interrupt();
					CMNF.Log("WorkerThread done.");
					if(bDisposable) CleanUp();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if(finallyDo!=null){
						try {
							Thread.sleep(400);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						bRunning = false;
						if(!stopped) {
							CMNF.Log("PlayService", "finallyDo!!!");
							finallyDo.doit(ExeCommand.this);
						}
					}
					//else stop();
				}
			});

            //if (bSynchronous) WorkerThread.run();
            //else WorkerThread.start();
			//WorkerThread.start();
			//WorkerThread.join();
			process.waitFor();
        } catch (Exception e) {
        	CMNF.Log(e);
			return -1;
        }
        return 0;
    }

	private void CleanUp() {
		try {
			errorResult.close();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean isAlive(Process process) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			return process.isAlive();
		}else try {
			process.exitValue();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/** Stop */
	public void stop() {
		stopped = true;
		finallyDo =null;
		Log.e("PlayService","stop1");
		try {
			 writer.write("q".getBytes());
	         writer.flush();
	         Log.e("PlayService","stop1.1");
	         if(writer !=null) {
		         writer.writeBytes("exit\n");
	             writer.flush();
	             Log.e("PlayService","stop1.2");
	         }
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
		    try {
				process.destroy();
				//writer.close();
				process.getOutputStream().close();
				Log.e("PlayService","stop1.31");
				process.getInputStream().close();
				Log.e("PlayService","stop1.32");
				//关闭不了？？
				//if(process.getErrorStream()!=null && process.getErrorStream().available()>=0)
				//	process.getErrorStream().close();
				//Log.e("PlayService","stop1.33");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		Log.e("PlayService","stop2");
		if(false)
	     try {
	    	 process.waitFor();
			 Thread.sleep(100);
		 } catch (Exception e) {
			 e.printStackTrace();
		 }
	     Log.e("PlayService","stop3");
	     if(false)
		try {
			Thread.sleep(500);
			Log.e("PlayService","stop3.1");
			process.destroy();
			Log.e("PlayService","stop3.2");
		} catch (Exception e) {
			e.printStackTrace();
		}

         try {
             int ret = process.exitValue();
             Log.e("PlayService","stop3.1");
         } catch (IllegalThreadStateException e) {
        	 Log.e("PlayService","stop3.2");
        	 //process.destroy();
        	 //Log.e("PlayService","stop3.3");
         } 
		
		Log.e("PlayService","stop4");

	}
	
}