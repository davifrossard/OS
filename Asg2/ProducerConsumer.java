import java.util.LinkedList;

/**
 * Classe que implementa uma fila paramétrica 
 * de Produtor/Consumidor.
 * 
 * @author Davi E. N. Frossard, Rodolfo V. Valentim
 *
 * @param <T>
 * 		Tipo de objetos da fila.
 */
public class ProducerConsumer<T> {
	/**
	 * Lista duplamente encadeada que conterá os produtos.
	 */
	private LinkedList<T> content;
	
	/**
	 * Tamanho máximo da lista.
	 */
	private int size;
	
	/**
	 * Modo verboso. Imprime informações sobre todas as
	 * operações de produção/consumo sobre a fila. 
	 * False por default.
	 */
	private boolean verbose = false;
	
	/**
	 * Instancia um objeto da classe ProdutorConsumidor
	 * 
	 * @param tam
	 * 		Tamanho máximo da fila.
	 */
	public ProducerConsumer(int tam)
	{
		content = new LinkedList<T>();
		size = tam;
	}
	
	/**
	 * Instancia um objeto da classe ProdutorConsumidor
	 * 
	 * @param tam
	 * 		Tamanho máximo da fila.
	 * @param verbosity
	 * 		Modo verboso
	 */
	public ProducerConsumer(int tam, boolean verbosity)
	{
		content = new LinkedList<T>();
		size = tam;
		verbose = verbosity;
	}
	

	/**
	 * Insere um objeto na fila.
	 * Inicialmente verifica se existe espaço disponível na fila.
	 * Então adiciona o objeto fornecido à fila e, caso a fila
	 * estivesse previamente vazia, notifica os objetos que por
	 * ventura estivessem esperando por produtos.
	 * 
	 * @param val
	 * 		Valor a ser inserido na fila.
	 * @param id
	 * 		Identificação do produtor.
	 * @throws InterruptedException
	 * 		Exceção de falha na interrupção.
	 */
	public synchronized void put(T val, String id) throws InterruptedException
	{
		while(content.size() >= size)
			this.wait();

		content.add(val);
		
		if(content.size() == 1)
			this.notifyAll();

		if(verbose) System.out.println(id + " produziu " + val + " (#" + content.size() + ")");
	}
	
	/**
	 * Remove um objeto da fila.
	 * Inicialmente verifica se existem produtos disponíveis. Consumindo
	 * então o primeiro da fila e, caso a fila estivesse previamente cheia,
	 * notifica os objetos que por ventura estivessem esperando por espaços.
	 * @param id
	 * 		Identificação do consumidor.
	 * @return
	 * 		O produto consumido.
	 * @throws InterruptedException
	 * 		Exceção de falha na interrupção.
	 */
	public synchronized T get(String id) throws InterruptedException
	{
		while(content.size() <= 0)
			this.wait();
		
		T res = content.remove();
		
		if(content.size() == 99)
			this.notifyAll();
		
		if(verbose) System.out.println(id + " consumiu " + res);
		return res;
	}
	
	/**
	 * Verifica o próximo objeto da fila.
	 * Apenas verifica objeto no topo da fila, sem consumí-lo ou
	 * travar caso a fila esteja vazia.
	 * @param id
	 * 		Identificação do consumidor.
	 * @return
	 * 		O produto no topo da fila.
	 * @throws InterruptedException
	 * 		Exceção de falha na interrupção.
	 */
	public synchronized T peek(String id) throws InterruptedException
	{
		T res = content.peek();
		if(verbose) System.out.println(id + " consultou " + res);
		return res;
	}
}
