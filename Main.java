package com.company;

import org.w3c.dom.Node;

import java.util.*;


/**
 * implementing nodes of a map's graph
 */
class MyNode implements Comparable<MyNode> {
    double x,y;
    double cost;
    MyNode parent;
    Edge toParent;
    String id;
    MyNode(double x, double y, String id){
        this.x=x;
        this.y=y;
        this.id=id;
        cost=Double.MAX_VALUE;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MyNode)) return false;
        MyNode node = (MyNode) o;
        return id.equals(node.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public int compareTo(MyNode o) {
        if(this.cost<o.cost) return -1;
        else if(this.cost==o.cost) return 0;
        else return 1;
    }
}

/**
 * implementing edges of a map's graph
 */
class Edge {
    MyNode n1, n2;
    double weight,distance;
    int traffic;
    Edge(MyNode n1, MyNode n2){
        this.n1=n1;
        this.n2=n2;
        this.traffic=0;
        distance= Math.sqrt((n1.x-n2.x)*(n1.x-n2.x)+(n1.y-n2.y)*(n1.y-n2.y));
        weight=distance*(1+0.3*traffic);
    }

    public void increaseTraffic(){
        traffic++;
        resetWidth();
    }
    public void decreaseTraffic(){
        traffic--;
        resetWidth();
    }

    public void resetWidth(){
        weight=distance*(1+0.3*traffic);
    }

}

/**
 * implementation of a minheap- using hashmap
 */
class MinHeap{
    ArrayList<MyNode> heap;
    HashMap<MyNode,Integer> mapNodesToIndexes;
    MinHeap(int size){
        heap=new ArrayList<>(size);
        mapNodesToIndexes=new HashMap<>();
    }
    public boolean contains(MyNode node){
        if(node==null)return false;
        else return mapNodesToIndexes.containsKey(node);
    }
    public void addNode(MyNode toAdd){
        heap.add(toAdd);
        int last=heap.size()-1;
        mapNodesToIndexes.put(toAdd,last);
        heapifyBottomUp(last);
    }
    public int size(){
        return heap.size();
    }
    public boolean isEmpty(){
        return size()==0;
    }
    public MyNode peek(){
        if(isEmpty()) return null;
        else return heap.get(0);
    }
    public MyNode poll(){
        return removeAt(0);
    }
    private void swap(int i, int j) {
        MyNode node_i = heap.get(i);
        MyNode node_j = heap.get(j);

        heap.set(i, node_j);
        heap.set(j, node_i);
        mapNodesToIndexes.remove(node_i);
        mapNodesToIndexes.remove(node_j);
        mapNodesToIndexes.put(node_i,j);
        mapNodesToIndexes.put(node_j,i);
    }
    private boolean lessEqual(int i, int j) {
        MyNode node1 = heap.get(i);
        MyNode node2 = heap.get(j);
        return (node1.compareTo(node2)) <= 0;
    }
    private void heapifyTopDown(int k) {
        int heapSize = size();
        while (true) {
            int left = 2 * k + 1,right = 2 * k + 2;
            int min = left;
            if (right < heapSize && lessEqual(right, left)) min = right;
            if (left >= heapSize || lessEqual(k, min)) break;
            swap(min, k);
            k = min;
        }
    }
    private void heapifyBottomUp(int k) {
        int parent = (k - 1) / 2;
        while (k > 0 && lessEqual(k, parent)) {
            swap(parent, k);
            k = parent;
            parent = (k - 1) / 2;
        }
    }
    public boolean remove(MyNode toRemove){
        if(toRemove==null) return false;
        else{
            Integer index=mapNodesToIndexes.get(toRemove);
            if(index==null) return false;
            else {
                removeAt(index);
                return true;
            }
        }
    }
    private MyNode removeAt(int index) {
        if (isEmpty()) return null;
        int indexOfLastElem = size() - 1;
        MyNode toRemove = heap.get(index);
        swap(index, indexOfLastElem);
        heap.remove(indexOfLastElem);
        mapNodesToIndexes.remove(toRemove);
        if (index == indexOfLastElem) return toRemove;
        MyNode elem = heap.get(index);
        heapifyTopDown(index);
        if (heap.get(index).equals(elem)) heapifyBottomUp(index);
        return toRemove;
    }
}

class Request {
    double startTime;
    double timeCost;
    double endTime;
    ArrayList<Edge> path;
    MyNode source;
    MyNode destination;
    boolean arrived;
    Request (double startTime , MyNode source, MyNode destination){
        this.startTime=startTime;
        this.source=source;
        this.destination=destination;
        path=new ArrayList<>();
    }
    public void setEndTime(){
        if(path==null){
            endTime=Double.MAX_VALUE;
        }
        else {
            double weight=0;
            for(Edge edge : path){
                if(edge==null){
                    System.out.println("edge null");
                }
                weight+=edge.weight;
                edge.increaseTraffic();
            }
            timeCost=120*weight;
            endTime=timeCost+startTime;
        }
    }
    public void arrive(){
        if(path!=null){
            for (Edge edge : path){
                edge.decreaseTraffic();
            }
            arrived=true;
        }
    }
}

public class Main {
    public static void dijkstra(HashMap<MyNode,LinkedList<Edge>> adj, Request request){
        MyNode source=request.source;
        MyNode destination=request.destination;
        MyNode curr= source;
        curr.cost=0;
        MinHeap heap=new MinHeap(adj.keySet().size());
        heap.addNode(curr);
        while (!heap.isEmpty()){
            curr=heap.poll();
            if(curr.id==destination.id){
                break;
            }
            for (Edge edge:adj.get(curr)){
                MyNode end;
                if(edge.n1.equals(curr)) end=edge.n2;
                else end=edge.n1;
                double weight=edge.weight;
                double cost=curr.cost+weight;
                if(cost<end.cost){
                    heap.remove(end);
                    end.cost=cost;
                    heap.addNode(end);
                    end.parent=curr;
                    end.toParent=edge;
                }
            }
        }
        if(destination.cost==Double.MAX_VALUE){
            request.path=null;
            request.setEndTime();
            System.out.println("there is no path");
        }
        else {
            String[] result=new String[2];
            double cost= destination.cost*120;
            result[1]=Double.toString(cost);
            result[0] = "";
            ArrayList<String> path = new ArrayList<>();
            MyNode node = destination;
            while (true){
                path.add(node.id);
                if(node.parent==null || node==source) break;
                else {node=node.parent;
                    if(node.toParent!=null) request.path.add(node.toParent);
                }
            }
            request.setEndTime();
            for (int i=path.size()-1;i>=0;i--){
                result[0]+=path.get(i);
                result[0]+=" ";
            }
            System.out.println("path : "+result[0]);
            System.out.println("cost : " +result[1]+" minutes");
        }
    }
    public static void main(String[] args) {
        HashMap<MyNode, LinkedList<Edge>> adj=new HashMap<>();
        HashMap<String, MyNode> mapIdTpNode=new HashMap<>();
        ArrayList<Request> requests = new ArrayList<>();
        int nodes, edges;
        Scanner scanner=new Scanner(System.in);
        nodes=scanner.nextInt();
        edges=scanner.nextInt();
        for (int i=0; i<nodes; i++){
            String id=scanner.next();
            double y=scanner.nextDouble();
            double x=scanner.nextDouble();
            MyNode node= new MyNode(x,y,id);
            mapIdTpNode.put(id,node);
            adj.put(node,new LinkedList<>());
        }
        for (int i=0; i<edges ;i++){
            String id1 = scanner.next();
            String id2=scanner.next();
            MyNode node1=mapIdTpNode.get(id1);
            MyNode node2=mapIdTpNode.get(id2);
            Edge edge = new Edge(node1,node2);
            adj.get(node1).add(edge);
            adj.get(node2).add(edge);
        }
        while (true){
            for(MyNode node : adj.keySet()){
                node.parent=null;
                node.toParent=null;
                node.cost=Double.MAX_VALUE;
            }
            double currentTime = scanner.nextDouble();
            Iterator<Request> iterator = requests.iterator();
            while (iterator.hasNext()){
                Request current = iterator.next();
                if(currentTime>current.endTime){
                    current.arrive();
                    iterator.remove();
                }
            }
            String sid=scanner.next();
            String did=scanner.next();
            Request request = new Request(currentTime,mapIdTpNode.get(sid),mapIdTpNode.get(did));
            requests.add(request);
            dijkstra(adj,request);
        }
    }

}
