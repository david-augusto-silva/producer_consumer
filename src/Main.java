import java.util.LinkedList;

public class Main{
    public static void main(String[] args) throws InterruptedException{
        
        final PC pc = new PC();

        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run(){
                try{
                    pc.produce();
                } catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        });

        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run(){
                try{
                    pc.consume();
                } catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        });

        t1.start(); 
        t2.start();
        
        t1.join();
        t2.join();
    }

    /*
    Contém o buffer compartilhado em forma de lista encadeada, 
    o produtor(alimenta o buffer) e o consumidor (remove itens do buffer)    
    */
    public static class PC{
        LinkedList<Integer> buffer = new LinkedList<>();
        int capacity=2; 

        public void produce() throws InterruptedException{
            int value = 0;
            while(true){
                synchronized (this){
                    if(buffer.size() == capacity){
                        System.out.println("Buffer cheio. Produtor em espera...");
                        notify();
                        wait();
                    }
                    buffer.add(value);
                    System.out.println("Produtor produziu "+value);
                    value++;
                    notify();
                    Thread.sleep(1000);
                }
            }
        }

        public void consume() throws InterruptedException{
            while (true){
                synchronized (this){
                    if (buffer.size() == 0){
                        System.out.println("Buffer vazio. Consumidor em espera...");
                        notify();
                        wait();
                    }

                    int val = buffer.removeFirst();
                    System.out.println("Consumidor consumiu-"+val);
                    notify();
                    Thread.sleep(1000);
                }
            }
        }
    }
}

