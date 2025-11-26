import org.jocl.*;

public class ParallelGPU {

    public static int countWordsGPU(String[] text, String palavraAlvo) {

        int targetHash = palavraAlvo.toLowerCase().hashCode();
        int n = text.length;

        int[] textHashes = new int[n];
        int[] result = new int[n];

        for (int i = 0; i < n; i++)
            textHashes[i] = text[i].toLowerCase().hashCode();

        CL.setExceptionsEnabled(true);

        // Plataforma
        cl_platform_id[] platforms = new cl_platform_id[1];
        CL.clGetPlatformIDs(1, platforms, null);

        // Dispositivo
        cl_device_id[] devices = new cl_device_id[1];
        CL.clGetDeviceIDs(platforms[0], CL.CL_DEVICE_TYPE_GPU, 1, devices, null);

        cl_context context = CL.clCreateContext(null, 1, devices, null, null, null);
        cl_command_queue queue = CL.clCreateCommandQueue(context, devices[0], 0, null);

        String kernelSource =
                "__kernel void wordCount(__global const int* text, "
                        + " __global int* result, const int targetHash) { "
                        + "   int gid = get_global_id(0); "
                        + "   result[gid] = (text[gid] == targetHash) ? 1 : 0; "
                        + "}";

        cl_program program = CL.clCreateProgramWithSource(context, 1, new String[]{kernelSource}, null, null);
        CL.clBuildProgram(program, 0, null, null, null, null);

        cl_kernel kernel = CL.clCreateKernel(program, "wordCount", null);

        cl_mem textBuffer = CL.clCreateBuffer(context,
                CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR,
                n * Sizeof.cl_int, Pointer.to(textHashes), null);

        cl_mem resultBuffer = CL.clCreateBuffer(context,
                CL.CL_MEM_WRITE_ONLY, n * Sizeof.cl_int, null, null);

        CL.clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(textBuffer));
        CL.clSetKernelArg(kernel, 1, Sizeof.cl_mem, Pointer.to(resultBuffer));
        CL.clSetKernelArg(kernel, 2, Sizeof.cl_int, Pointer.to(new int[]{targetHash}));

        long[] globalWorkSize = new long[]{n};

        CL.clEnqueueNDRangeKernel(queue, kernel, 1, null, globalWorkSize, null, 0, null, null);

        CL.clEnqueueReadBuffer(queue, resultBuffer, CL.CL_TRUE, 0, n * Sizeof.cl_int, Pointer.to(result),
                0, null, null);

        CL.clReleaseMemObject(textBuffer);
        CL.clReleaseMemObject(resultBuffer);
        CL.clReleaseKernel(kernel);
        CL.clReleaseProgram(program);
        CL.clReleaseCommandQueue(queue);
        CL.clReleaseContext(context);

        int total = 0;
        for (int v : result) total += v;

        return total;
    }
}
