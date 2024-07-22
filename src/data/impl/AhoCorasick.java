package data.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class AhoCorasick {
    private final Node root;

    public AhoCorasick() {
        root = new Node();
    }

    public void addKeyword(String keyword) {
        Node node = root;
        for (char ch : keyword.toCharArray()) {
            node = node.getChildren().computeIfAbsent(ch, k -> new Node());
        }
        node.setLeaf(true);
    }

    public void prepare() {
        Queue<Node> queue = new LinkedList<>();
        for (Node node : root.getChildren().values()) {
            node.setFailure(root);
            queue.add(node);
        }

        while (!queue.isEmpty()) {
            Node current = queue.poll();

            for (Map.Entry<Character, Node> entry : current.getChildren().entrySet()) {
                char transition = entry.getKey();
                Node target = entry.getValue();

                Node failure = current.getFailure();
                while (failure != null && !failure.getChildren().containsKey(transition)) {
                    failure = failure.getFailure();
                }
                if (failure == null) {
                    target.setFailure(root);
                } else {
                    target.setFailure(failure.getChildren().get(transition));
                }
                queue.add(target);
            }
        }
    }

    public List<Integer> search(String text) {
        List<Integer> result = new ArrayList<>();
        Node node = root;

        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);

            while (node != null && !node.getChildren().containsKey(ch)) {
                node = node.getFailure();
            }
            if (node == null) {
                node = root;
                continue;
            }

            node = node.getChildren().get(ch);
            if (node.isLeaf()) {
                result.add(i);
            }
        }

        return result;
    }

    private static class Node {
        private final Map<Character, Node> children = new HashMap<>();
        private Node failure;
        private boolean isLeaf;

        public Map<Character, Node> getChildren() {
            return children;
        }

        public Node getFailure() {
            return failure;
        }

        public void setFailure(Node failure) {
            this.failure = failure;
        }

        public boolean isLeaf() {
            return isLeaf;
        }

        public void setLeaf(boolean leaf) {
            isLeaf = leaf;
        }
    }
}
