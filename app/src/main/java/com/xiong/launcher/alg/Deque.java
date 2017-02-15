package com.xiong.launcher.alg;

import java.util.Iterator;
import java.util.LinkedList;

public class Deque<T> {
	private LinkedList<T> deque = new LinkedList<T>();
	
	public void add(T t){
		deque.add(t);
	}
	
	public void add(int index,T t){
		deque.add(index, t);
	}
	
	public void remove(T t){
		deque.remove(t);
	}
	
	public void addFist(T t){
		deque.addFirst(t);
	}
	
	public void addLast(T t){
		deque.addLast(t);
	}
	
	public T getFist(){
		return deque.getFirst();
	}
	
	public T getLast(){
		return deque.getLast();
	}
	
	public T removeFist(){
		return deque.removeFirst();
	}
	
	public T removeLast(){
		return deque.removeLast();
	}
	
	public int size(){
		return deque.size();
	}

	@Override
	public String toString() {
		return deque.toString();
	}
	
	public Iterator<T> iterator(){
		return deque.iterator();
	}
	
	public T get(int location){
		return deque.get(location);
	}
	
	public void clearAll(){
		deque.clear();
	}

}
