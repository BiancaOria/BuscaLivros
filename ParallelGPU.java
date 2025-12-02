import org.jocl.*;
import java.util.Arrays;

public class ParallelGPU {

    // Variáveis estáticas para manter a "fábrica" aberta
    private static cl_context context;
    private static cl_command_queue queue;
    private static cl_kernel kernel;
    private static cl_program program;
    private static boolean isInitialized = false;

    private static final String KERNEL_SOURCE =
            "__kernel void searchAndReduce(__global const int* textHashes, " +
                    "                              __global int* partialSums, " +
                    "                              const int targetHash, " +
                    "                              const int n, " +
                    "                              __local int* localSums) { " +
                    "   int globalId = get_global_id(0); " +
                    "   int localId = get_local_id(0); " +
                    "   int groupSize = get_local_size(0); " +
                    "   int val = 0; " +
                    "   if (globalId < n) { " +
                    "       val = (textHashes[globalId] == targetHash) ? 1 : 0; " +
                    "   } " +
                    "   localSums[localId] = val; " +
                    "   barrier(CLK_LOCAL_MEM_FENCE); " +
                    "   for (int stride = groupSize / 2; stride > 0; stride /= 2) { " +
                    "       if (localId < stride) { " +
                    "           localSums[localId] += localSums[localId + stride]; " +
                    "       } " +
                    "       barrier(CLK_LOCAL_MEM_FENCE); " +
                    "   } " +
                    "   if (localId == 0) { " +
                    "       partialSums[get_group_id(0)] = localSums[0]; " +
                    "   } " +
                    "}";

    // CHAME ISSO APENAS UMA VEZ NO INICIO DO MAIN
    public static void init() {
        if (isInitialized) return;

        CL.setExceptionsEnabled(true);
        cl_platform_id[] platforms = new cl_platform_id[1];
        CL.clGetPlatformIDs(1, platforms, null);
        cl_device_id[] devices = new cl_device_id[1];
        CL.clGetDeviceIDs(platforms[0], CL.CL_DEVICE_TYPE_GPU, 1, devices, null);

        context = CL.clCreateContext(null, 1, devices, null, null, null);
        queue = CL.clCreateCommandQueue(context, devices[0], 0, null);

        program = CL.clCreateProgramWithSource(context, 1, new String[]{KERNEL_SOURCE}, null, null);
        CL.clBuildProgram(program, 0, null, null, null, null);
        kernel = CL.clCreateKernel(program, "searchAndReduce", null);

        isInitialized = true;
        System.out.println(">>> GPU Inicializada e Driver Carregado.");
    }

    // CHAME ISSO APENAS UMA VEZ NO FIM DO MAIN
    public static void close() {
        if (!isInitialized) return;
        CL.clReleaseKernel(kernel);
        CL.clReleaseProgram(program);
        CL.clReleaseCommandQueue(queue);
        CL.clReleaseContext(context);
        isInitialized = false;
    }

    // O método de contagem agora só transfere dados e executa (MUITO RÁPIDO)
    public static int countWordsGPU(String[] text, String palavraAlvo) {
        if (!isInitialized) throw new RuntimeException("Chame ParallelGPU.init() primeiro!");

        int targetHash = palavraAlvo.toLowerCase().hashCode();
        int n = text.length;

        // Conversão (Ainda na CPU, mas paralela)
        int[] textHashes = Arrays.stream(text).parallel().mapToInt(String::hashCode).toArray();

        // Configuração de Threads
        int localSize = 256;
        int numGroups = (int) Math.ceil((double) n / localSize);
        long globalSize = numGroups * localSize;

        // Alocação de Buffers (Isso é rápido)
        cl_mem inputBuffer = CL.clCreateBuffer(context, CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR,
                Sizeof.cl_int * n, Pointer.to(textHashes), null);

        int[] partialSums = new int[numGroups];
        cl_mem outputBuffer = CL.clCreateBuffer(context, CL.CL_MEM_WRITE_ONLY,
                Sizeof.cl_int * numGroups, null, null);

        // Argumentos
        CL.clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(inputBuffer));
        CL.clSetKernelArg(kernel, 1, Sizeof.cl_mem, Pointer.to(outputBuffer));
        CL.clSetKernelArg(kernel, 2, Sizeof.cl_int, Pointer.to(new int[]{targetHash}));
        CL.clSetKernelArg(kernel, 3, Sizeof.cl_int, Pointer.to(new int[]{n}));
        CL.clSetKernelArg(kernel, 4, Sizeof.cl_int * localSize, null);

        // Execução
        CL.clEnqueueNDRangeKernel(queue, kernel, 1, null, new long[]{globalSize}, new long[]{localSize}, 0, null, null);

        // Leitura
        CL.clEnqueueReadBuffer(queue, outputBuffer, CL.CL_TRUE, 0, Sizeof.cl_int * numGroups, Pointer.to(partialSums), 0, null, null);

        // Limpeza dos buffers desta execução (Contexto e Kernel continuam vivos)
        CL.clReleaseMemObject(inputBuffer);
        CL.clReleaseMemObject(outputBuffer);

        int total = 0;
        for (int p : partialSums) total += p;
        return total;
    }
}