package com.zxy.combinearea;

import android.graphics.Rect;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by zhouxiangyu on 2018/1/23.
 */

public class Combine {

    private final String TAG = "zhouxiangyu";
    private final int LEFT = 0;
    private final int TOP = 1;
    private final int RIGHT = 2;
    private final int BOTTOM = 3;

    private int nextDirection = LEFT;
    private boolean isFirstTime = true;
    private boolean interrupt = false;
    private boolean quickMode = false;

    private ArrayList<Integer> ys = new ArrayList<>();
    private ArrayList<Integer> xs = new ArrayList<>();

    private ArrayList<Rect> refreshAreas = new ArrayList<>();
    private ArrayList<Node> nodes = new ArrayList<>();
    private ArrayList<Node> walls = new ArrayList<>();
    private ArrayList<Rect> areas = new ArrayList<>();
    private ArrayList<Node> currentArea = new ArrayList<>();
    private ArrayList<Node> childrenToAdd = new ArrayList<>();
    private Node currentNode;
    private int xs_size, ys_size;
    private int nodesSize;
    private Rect[] rects;


    Combine(Rect[] rects, int areaWidth, int areaHeight) {
        this(rects, areaWidth, areaHeight, false);
    }

    Combine(Rect[] rects, int areaWidth, int areaHeight, boolean quickMode) {
        init(rects, areaWidth, areaHeight, quickMode);
    }

    public ArrayList<Rect> getResult() {
        if (refreshAreas.size() != 0) {
            return refreshAreas;
        }
        return null;
    }

    private void init(Rect[] rects, int areaWidth, int areaHeight, boolean quickMode) {
        this.quickMode = quickMode;
        if (rects.length != 0) {
            this.rects = rects;
        } else {
            Log.e(TAG, "Rect can't be null !");
            return;
        }
        for (Rect rect : rects) {
            xs.add(rect.left);
            xs.add(rect.right);
            ys.add(rect.top);
            ys.add(rect.bottom);
        }
        //将区域边框加入排序
        ys.add(0);
        ys.add(areaWidth);
        xs.add(0);
        xs.add(areaHeight);
        bubbleSort(xs);
        bubbleSort(ys);
        xs_size = xs.size();
        ys_size = ys.size();
        long time = System.nanoTime();
        initAreas();
        double delta = (System.nanoTime() - time) / 1000000.0;
        Log.d(TAG, "Time Cost: " + delta + " ms");
    }

    private void initAreas() {
        Node node;
        for (int i = 0; i < ys_size - 1; i++) {
            for (int j = 0; j < xs_size - 1; j++) {
                Rect area = new Rect();
                area.top = ys.get(i);
                area.bottom = ys.get(i + 1);
                area.left = xs.get(j);
                area.right = xs.get(j + 1);
                node = new Node(j, i);
                if (!areaInRect(area)) {
                    nodes.add(node);
                } else {
                    walls.add(node);
                }
            }
        }
        Log.d(TAG, "nodes num : " + nodes.size());
        //开始合并矩形
        nodesSize = nodes.size();
        combineNearbyAreas();
    }

    private void combineNearbyAreas() {
        //初始时解的最小矩形数目为未使用区域数
        int minNum = nodesSize;
        for (int i = 0; i < 1; i++) {
            interrupt = false;
            //初始化解列表
            areas.clear();
            //初始化所有节点为未使用状态
            changeToNotUsed();
            //初始化当前需合并矩形列表
            currentArea.clear();
            //将第一个节点作为初始节点
            currentNode = nodes.get(i);
            //将初始节点存入矩形列表
            currentArea.add(currentNode);
            //如果已使用节点数小于所有节点数，则循环在当前矩形列表基础上扩充矩形
            while (getUsedNodeNum() <= nodesSize && currentNode != null) {

                //当前解已经大于当前最优解，清除当前数据，结束剩余循环
                if (areas.size() > minNum) {
                    currentNode = null;
                    interrupt = true;
                    break;
                }

                //判断初始节点有无左孩子，如果有则进行深度遍历，然后纵向扩展矩形列表使其包含的矩形最多
                if (nextDirection == LEFT) {
                    if (hasChild(currentNode, LEFT)) {
                        getAndSaveNode(LEFT);
                    } else {
                        //如果当前方向不可扩展，切换方向
                        if (isFirstTime) {
                            nextDirection = RIGHT;
                        } else {
                            //纵向扩展矩形列表
                            getChildren(currentArea, TOP);
                            getChildren(currentArea, BOTTOM);
                            //当前矩形已最大，将currentNode置为未使用的第一个矩形,清除矩形列表
                            //遍历方向变为左，重置相关标志位，开始新一轮循环
                            initNode();
                        }
                    }
                } else if (nextDirection == RIGHT) {
                    if (hasChild(currentNode, RIGHT)) {
                        getAndSaveNode(RIGHT);
                    } else {
                        if (isFirstTime) {
                            nextDirection = TOP;
                        } else {
                            getChildren(currentArea, TOP);
                            getChildren(currentArea, BOTTOM);
                            initNode();
                        }
                    }
                } else if (nextDirection == TOP) {
                    if (hasChild(currentNode, TOP)) {
                        getAndSaveNode(TOP);
                    } else {
                        if (isFirstTime) {
                            nextDirection = BOTTOM;
                        } else {
                            getChildren(currentArea, LEFT);
                            getChildren(currentArea, RIGHT);
                            initNode();
                        }
                    }

                } else if (nextDirection == BOTTOM) {
                    if (hasChild(currentNode, BOTTOM)) {
                        getAndSaveNode(BOTTOM);
                    } else {
                        //如果初始节点的四个方向都不可扩充，则将其作为一个成功和并的区域存入解列表
                        if (isFirstTime) {
                            nextDirection = LEFT;
                            currentNode.used = true;
                            areas.add(getRect(currentArea));
                            currentNode = getFirstUnUsedNode();
                        } else {
                            getChildren(currentArea, LEFT);
                            getChildren(currentArea, RIGHT);
                            initNode();
                        }
                    }
                }

            }
            if (!interrupt) {
                //一次循环结束，当前解为最优解
                minNum = areas.size();
                refreshAreas.clear();
                refreshAreas.addAll(areas);
            }
        }
        Log.d(TAG, "areas num : " + areas.size());
    }

    private Rect getRect(ArrayList<Node> currentArea) {
        Rect refreshArea = new Rect();
        for (Node node : currentArea) {
            refreshArea.union(xs.get(node.x), ys.get(node.y), xs.get(node.x + 1), ys.get(node.y + 1));
        }
        return refreshArea;
    }

    private void initNode() {
        currentArea.addAll(childrenToAdd);
        areas.add(getRect(currentArea));
        changeToUsed(currentArea);
        currentArea.clear();
        childrenToAdd.clear();
        currentNode = getFirstUnUsedNode();
        if (currentNode != null) {
            currentArea.add(currentNode);
            currentNode.used = true;
        }
        nextDirection = LEFT;
        isFirstTime = true;
    }

    private void getAndSaveNode(int direction) {
        currentNode = getChild(currentNode, direction);
        currentArea.add(currentNode);
        currentNode.used = true;
        isFirstTime = false;
    }

    private void getChildren(ArrayList<Node> currentArea, int direction) {
        ArrayList<Node> children = new ArrayList<>();
        Node oneChild;
        for (Node node : currentArea) {
            if (direction == LEFT) {
                oneChild = new Node(node.x - 1, node.y);
            } else if (direction == RIGHT) {
                oneChild = new Node(node.x + 1, node.y);
            } else if (direction == TOP) {
                oneChild = new Node(node.x, node.y - 1);
            } else if (direction == BOTTOM) {
                oneChild = new Node(node.x, node.y + 1);
            } else {
                oneChild = null;
            }
            if (oneChild != null && !((node.x < 0 || node.x > xs_size - 2) || (node.y < 0 || node.y > ys_size - 2))) {
                if (nodeInNodeList(oneChild)) {
                    children.add(oneChild);
                } else {
                    break;
                }
            } else {
                break;
            }
        }
        if (children.size() == currentArea.size()) {
            childrenToAdd.addAll(children);
            changeToUsed(children);
            getChildren(children, direction);
        }
    }

    private int getUsedNodeNum() {
        int num = 0;
        for (int i = 0; i < nodesSize; i++) {
            if (nodes.get(i).used) {
                num++;
            }
        }
        return num;
    }

    private boolean nodeInNodeList(Node node) {
        int size = walls.size();
        for (int i = 0; i < size; i++) {
            //由于区域的设置可重叠，并且最后设置的矩形越少越好，所以可将已使用区域用于扩充
            //这样一定概率可以提前消耗未使用矩形，快速得到数量最少的解
            if ((node.equals(walls.get(i)))
                    || (node.x < 0 || node.x > xs_size - 2)
                    || (node.y < 0 || node.y > ys_size - 2)) {
                return false;
            }
        }
        if (!quickMode){
            for (int i = 0; i < nodesSize; i++) {
                if(nodes.get(i).equals(node)){
                    if(nodes.get(i).used) {
                        return false;
                    }
                }
            }
        }
            return true;
    }

    private Node getFirstUnUsedNode() {
        for (int i = 0; i < nodesSize; i++) {
            if (!nodes.get(i).used) {
                return nodes.get(i);
            }
        }
        return null;
    }

    private void changeToUsed(ArrayList<Node> areas) {
        for (Node node : areas) {
            for (Node listNode : nodes) {
                if (node.equals(listNode)) {
                    listNode.used = true;
                    break;
                }
            }
        }
    }

    private void changeToNotUsed() {
        for (Node node : nodes) {
            node.used = false;
        }
    }

    private boolean hasChild(Node node, int direction) {
        for (int i = 0; i < nodesSize; i++) {
            if (!nodes.get(i).used) {
                if (direction == LEFT) {
                    if (nodes.get(i).x == node.x - 1 && nodes.get(i).y == node.y) {
                        return true;
                    }
                } else if (direction == RIGHT) {
                    if (nodes.get(i).x == node.x + 1 && nodes.get(i).y == node.y) {
                        return true;
                    }
                } else if (direction == TOP) {
                    if (nodes.get(i).x == node.x && nodes.get(i).y == node.y - 1) {
                        return true;
                    }
                } else if (direction == BOTTOM) {
                    if (nodes.get(i).x == node.x && nodes.get(i).y == node.y + 1) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private Node getChild(Node node, int direction) {
        Node child;
        if (direction == LEFT) {
            child = new Node(node.x - 1, node.y);
            child.used = true;
            return child;
        } else if (direction == RIGHT) {
            child = new Node(node.x + 1, node.y);
            child.used = true;
            return child;
        } else if (direction == TOP) {
            child = new Node(node.x, node.y - 1);
            child.used = true;
            return child;
        } else if (direction == BOTTOM) {
            child = new Node(node.x, node.y + 1);
            child.used = true;
            return child;
        }
        return null;
    }

    class Node {
        Node(int x, int y) {
            this.x = x;
            this.y = y;
        }

        boolean used = false;
        int x;
        int y;

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null) return false;
            if (!(obj instanceof Node)) return false;
            Node node = (Node) obj;
            return node.x == this.x && node.y == this.y;
        }
    }

    private boolean areaInRect(Rect area) {
        boolean contains = false;
        for (Rect rect : rects) {
            contains = rect.contains(area) || contains;
        }
        return contains;
    }

    public static void bubbleSort(ArrayList<Integer> list) {
        int temp;
        int size = list.size();
        for (int i = 0; i < size - 1; i++) {
            for (int j = 0; j < size - 1 - i; j++) {
                if (list.get(j) > list.get(j + 1)) {
                    temp = list.get(j);
                    list.set(j, list.get(j + 1));
                    list.set(j + 1, temp);
                }
            }
        }

        //remove the same value
        ArrayList<Integer> newList = new ArrayList<>();
        for (Integer a : list) {
            if (!newList.contains(a)) {
                newList.add(a);
            }
        }
        list.clear();
        list.addAll(newList);
    }
}
