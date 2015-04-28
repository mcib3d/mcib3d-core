package tango.util;
/*import java.util.concurrent.atomic.*;
import static jcuda.driver.JCudaDriver.*;
import jcuda.driver.*;
import ij.*;

 * 
 */
public class CudaDeviceRouter {
    /*static CudaDeviceRouter instance;
    static int nDevices;
    static CUdevice[] devices;
    static CUcontext[] contexts;
    static AtomicIntegerArray running;
    //AtomicInteger curDev;

    private CudaDeviceRouter(){
        int[] nd = new int[1];
        JCudaDriver.setExceptionsEnabled(true);
        cuInit(0);
        cuDeviceGetCount(nd);
        //nDevices = nd[0];
        nDevices=1;
        devices=new CUdevice[nDevices];
        contexts=new CUcontext[nDevices];
        //curDev=new AtomicInteger(0);
        running = new AtomicIntegerArray(nDevices);

        for (int d = 0; d<nDevices; d++) {
           devices[d] = new CUdevice();
           cuDeviceGet(devices[d], d);
           contexts[d] = new CUcontext();
           //cuCtxCreate(contexts[d], jcuda.driver.CUctx_flags.CU_CTX_BLOCKING_SYNC, devices[d]);
           //cuCtxPopCurrent(null);
           //cuCtxDetach(contexts[d]);
        }
    }

    public static CudaDeviceRouter getInstance(){
        if (instance==null) {
            instance=new CudaDeviceRouter();
        }
        return instance;
    }
    
    public int getContext(int tidx) {
        int dev_nb=0;
        while(!running.weakCompareAndSet(dev_nb, 0, 1)) {
            dev_nb++;
            dev_nb%=nDevices;
            //try{wait(1);}
            //catch (Exception e) {IJ.log(e.getMessage());}
        }
        //cuCtxAttach(contexts[dev_nb], 0);
        //IJ.log("thread:"+tidx+" context taken:"+dev_nb);
        contexts[dev_nb] = new CUcontext();
        cuCtxCreate(contexts[dev_nb], 0, devices[dev_nb]);
        //cuCtxPushCurrent(contexts[dev_nb]);
        //cuCtxSynchronize();
        return dev_nb;
    }

    public void freeContext(int ctx_nb, int tidx, int count) {
        for (int idx=0; idx<count; idx++) cuCtxDetach(contexts[ctx_nb]);
        
        //CUcontext pctx = new CUcontext();
        //cuCtxPopCurrent(pctx);
        //cuCtxDestroy(contexts[ctx_nb]);
        running.set(ctx_nb, 0);
        //IJ.log("Thread:"+tidx+" context freed:"+ctx_nb);
    }

    public CUdevice getDevice(CUcontext ctx){
        int dev_nb=0;
        if (nDevices>=1) {
            dev_nb=curDev.getAndIncrement();
            dev_nb%=nDevices;
        }
        cuCtxCreate(ctx, jcuda.driver.CUctx_flags.CU_CTX_BLOCKING_SYNC, devices[dev_nb]); //POP???
        cuCtxSynchronize();
        return devices[dev_nb];
    }

    public String getMem () {
        long[] free = new long[1];
        long[] total = new long[1];
        cuMemGetInfo(free, total);
        float total_ = (float)total[0]/1000000f;
        float free_ = (float)free[0]/1000000f;
        return "Memory: total:"+total_+ "Mb free:"+free_+"Mb";
    }

    
     * 
     */
    

}
