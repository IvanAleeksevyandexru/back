package ru.gosuslugi.pgu.fs.common.jsonlogic.structure;

import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

@RequiredArgsConstructor
public class ArrayNode implements Node, List<Node> {

    private final List<Node> delegate;

    @Override
    public NodeType getType() {
        return NodeType.ARRAY;
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return delegate.contains(o);
    }

    @Override
    public Iterator<Node> iterator() {
        return delegate.iterator();
    }

    @Override
    public Object[] toArray() {
        return delegate.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return delegate.toArray(a);
    }

    @Override
    public boolean add(Node node) {
        throw new UnsupportedOperationException("json-logic arrays are immutable");

    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("json-logic arrays are immutable");
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return delegate.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends Node> c) {
        throw new UnsupportedOperationException("json-logic arrays are immutable");
    }

    @Override
    public boolean addAll(int index, Collection<? extends Node> c) {
        throw new UnsupportedOperationException("json-logic arrays are immutable");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("json-logic arrays are immutable");
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("json-logic arrays are immutable");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("json-logic arrays are immutable");
    }

    @Override
    public Node get(int index) {
        return delegate.get(index);
    }

    @Override
    public Node set(int index, Node element) {
        throw new UnsupportedOperationException("json-logic arrays are immutable");
    }

    @Override
    public void add(int index, Node element) {
        throw new UnsupportedOperationException("json-logic arrays are immutable");
    }

    @Override
    public Node remove(int index) {
        throw new UnsupportedOperationException("json-logic arrays are immutable");
    }

    @Override
    public int indexOf(Object o) {
        return delegate.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return delegate.lastIndexOf(o);
    }

    @Override
    public ListIterator<Node> listIterator() {
        return delegate.listIterator();
    }

    @Override
    public ListIterator<Node> listIterator(int index) {
        return delegate.listIterator(index);
    }

    @Override
    public List<Node> subList(int fromIndex, int toIndex) {
        return delegate.subList(fromIndex, toIndex);
    }
}
