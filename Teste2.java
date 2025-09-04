import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

/*
 * Referências:
 *  - https://www.geeksforgeeks.org/java/producer-consumer-solution-using-threads-java/
 * Trechos de código gerados por IA (Deepseek V3.1) 
 */

public class Teste2
{
    private static final int NUM_PRODUCERS=6,
                             NUM_CONSUMERS=3,
                             BUFFER_CAPACITY=6;

    public static void main(String[] args) throws InterruptedException{
        final PC pc = new PC(BUFFER_CAPACITY);
        final long startTime = System.nanoTime();

        Thread[] producers = new Thread[NUM_PRODUCERS];
        Thread[] consumers = new Thread[NUM_CONSUMERS];
        System.out.println("----------------CONFIGURAÇÕES-----------------");
        System.out.println("Tamanho do buffer: " + BUFFER_CAPACITY + 
                           "\nNúmero de threads produtoras:" + NUM_CONSUMERS + 
                           "\nNúmero de threads consumidoras: " + NUM_PRODUCERS);
        System.out.println("----------------------------------------------\n");
        
        for(int i=0; i<NUM_PRODUCERS; i++){
            final int producerId = i;
            producers[i] = new Thread(()->{
                try{
                    pc.produce(producerId);
                } catch (InterruptedException e){
                    Thread.currentThread().interrupt();
                }
            }, "Producer-"+i);
            producers[i].start(); 
        }

        for(int i=0; i<NUM_CONSUMERS; i++){
            final int consumerId = i;
            consumers[i] = new Thread(()->{
                try{
                    pc.consume(consumerId);
                } catch (InterruptedException e){
                    Thread.currentThread().interrupt();
                }
            }, "Consumer-"+i);
            consumers[i].start(); 
        }

        Thread.sleep(5000);

        for (Thread producer : producers) {
            producer.interrupt();
        }
        for (Thread consumer : consumers) {
            consumer.interrupt();
        }   

        for (Thread producer : producers) {
            producer.join();
        }
        for (Thread consumer : consumers) {
            consumer.join();
        }

        final long endTime = System.nanoTime();
        final long duration = (endTime - startTime) / 1_000_000;
        System.out.println("\n\nTempo de execução: "+duration+" ms");
    }

    public static class PC
    {
        /*
         * A classe PC possui os métodos produtor (produce()), que alimenta o buffer,
         * e consumidor (consumer()), que retira itens do buffer.
         */

        //Buffer compartilhado implementado como lista encadeada
        private final LinkedList<Integer> buffer = new LinkedList<>();

        //Capacidade do buffer
        private final int capacity;

        //Contador thread-safe
        private final AtomicInteger counter = new AtomicInteger(0);
    
        public PC(int capacity)
        {
            this.capacity = capacity;
        }

        public void produce(int producerID) throws InterruptedException
        {   while(!Thread.currentThread().isInterrupted()){
                synchronized(this){
                    while(buffer.size() == capacity){
                        System.out.println("Produtor " + producerID + " em espera - Buffer cheio (" + buffer.size() + "/" + capacity + ")");
                        if (Thread.currentThread().isInterrupted()) return;
                    }
                    int value = counter.getAndIncrement();
                    buffer.add(value);
                    System.out.println("Produtor " + producerID + " produziu: " + value + " (Buffer: " + buffer.size() + "/" + capacity + ")");
                    notifyAll();
                }
            }
            Thread.sleep(100+(int)(Math.random()*4));
        }

        public void consume(int consumerID) throws InterruptedException
        {   while (!Thread.currentThread().isInterrupted()){
                synchronized(this){
                    while(buffer.isEmpty()){
                        System.out.println("Consumidor "+consumerID+" em espera - Buffer vazio");
                        if (Thread.currentThread().isInterrupted()) return;
                    }

                    int value = buffer.removeFirst();
                    System.out.println("Consumidor "+consumerID+" consumiu: "+value+" (Buffer: " + buffer.size() + "/" + capacity +")");
                    notifyAll();
                }
                Thread.sleep(100+(int)(Math.random()*400));
            }
        }
    }
}
