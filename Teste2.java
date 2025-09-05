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
                             BUFFER_CAPACITY=3;

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
            // inicializa a Thread i implementando a interface Runnable por meio de expressão lambda
            producers[i] = new Thread(()->{
                try{
                    pc.produce(producerId);
                } catch (InterruptedException e){
                    Thread.currentThread().interrupt();
                }
            }, "Producer-"+(i+1));
            producers[i].start(); 
        }

        for(int i=0; i<NUM_CONSUMERS; i++){
            final int consumerId = i;
            // inicializa a Thread i implementando a interface Runnable por meio de expressão lambda
            consumers[i] = new Thread(()->{
                try{
                    pc.consume(consumerId);
                } catch (InterruptedException e){
                    Thread.currentThread().interrupt();
                }
            }, "Consumer-"+(i+1));
            consumers[i].start(); 
        }

        //Interrompe todas as threads após 5 segundos
        Thread.sleep(5000);
        for (Thread producer : producers) {
            producer.interrupt();
            System.out.println(producer.getName()+" interrompido");
        }
        for (Thread consumer : consumers) {
            consumer.interrupt();
            System.out.println(consumer.getName()+" interrompido");
        }
            

        //Espera as thread terminar
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
        private final AtomicInteger counter = new AtomicInteger(1);
    
        public PC(int capacity)
        {
            this.capacity = capacity;
        }

        public void produce(int producerID) throws InterruptedException
        {   while(!Thread.currentThread().isInterrupted()){
                synchronized(this){
                    while(buffer.size() == capacity){
                        System.out.println("Produtor " + producerID + " em espera - Buffer cheio (" + buffer.size() + "/" + capacity + ")");
                        wait();
                        Thread.sleep(200);
                        if (Thread.currentThread().isInterrupted()) return;
                    }
                    int value = counter.getAndIncrement();
                    buffer.add(value);
                    System.out.println("Produtor " + producerID + " produziu: " + value + " (Buffer: " + buffer.size() + "/" + capacity + ")");
                    Thread.sleep(500);
                    notifyAll();
                }
            }

            //
            Thread.sleep(1000);
        }

        public void consume(int consumerID) throws InterruptedException
        {   while (!Thread.currentThread().isInterrupted()){
                synchronized(this){
                    while(buffer.isEmpty()){
                        System.out.println("Consumidor "+consumerID+" em espera - Buffer vazio");
                        wait();
                        Thread.sleep(200);
                        if (Thread.currentThread().isInterrupted()) return;
                    }

                    int value = buffer.removeFirst();
                    System.out.println("Consumidor "+consumerID+" consumiu: "+value+" (Buffer: " + buffer.size() + "/" + capacity +")");
                    Thread.sleep(500);
                    notifyAll();
                }
                Thread.sleep(1000);
            }
        }
    }
}
