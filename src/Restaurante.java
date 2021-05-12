
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 *
 * @author jgimitola, jhanu, anietom
 */
public class Restaurante {
    
    public static int NUM_FILOSOFOS = 4;
    
    public static void main(String[] args) throws InterruptedException {
        Tenedor[] tenedores = new Tenedor[NUM_FILOSOFOS];
        
        Semaphore comedor = new Semaphore(NUM_FILOSOFOS - 1);
        
        IntStream.range(0, NUM_FILOSOFOS)
                .forEach((int i) -> tenedores[i] = new Tenedor(i, new Semaphore(1)));
        
        ExecutorService executor = Executors.newCachedThreadPool();
        for (int i = 0; i < NUM_FILOSOFOS; i++) {
            executor.submit(new Filosofo(i, comedor, tenedores));
        }
        executor.shutdown();
        //executor.awaitTermination(1, TimeUnit.DAYS);
      
    }
}
