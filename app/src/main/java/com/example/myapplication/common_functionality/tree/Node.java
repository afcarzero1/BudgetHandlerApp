package com.example.myapplication.common_functionality.tree;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Node<T> {

	private T data = null; // Data hold by the tree

	private List<Node<T>> children = new LinkedList<>();

	private Node<T> parent = null;

	public Node(T data) {
		this.data = data;
	}

	public Node<T> addChild(Node<T> child) {
		child.setParent(this);
		this.children.add(child);
		return child;
	}

	@RequiresApi(api = Build.VERSION_CODES.N)
	public void addChildren(List<Node<T>> children) {
		children.forEach(each -> each.setParent(this));
		this.children.addAll(children);
	}

	public List<Node<T>> getChildren() {
		return children;
	}

	public List<Node<T>> getAllChildren(){

		List<Node<T>> nodeList = new LinkedList<Node<T>>();


		for(Node<T> child : children){
			nodeList.addAll(child.getChildren());
		}

		return nodeList;
	}

	public Node<T> findChild(T data){

		for( Node<T> child : children){
			if(child.getData().equals(data)){
				//Found in direct child
				return child;
			}
		}

		// If it is not a direct child then
		for(Node<T> child : children){
			Node<T> childResult = child.findChild(data);
			if(childResult!=null){
				return childResult;
			}
		}

		// If not found return null
		return null;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	private void setParent(Node<T> parent) {
		this.parent = parent;
	}

	public Node<T> getParent() {
		return parent;
	}

	public boolean isLeaf(){
		return children.size()==0;
	}

}