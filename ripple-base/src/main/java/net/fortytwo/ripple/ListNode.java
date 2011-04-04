/*
 * $URL$
 * $Revision$
 * $Author$
 *
 * Copyright (C) 2007-2010 Joshua Shinavier
 */


package net.fortytwo.ripple;

/**
 * Head of a linked list.
 */
public abstract class ListNode<T> {
    public abstract T getFirst();

    public abstract ListNode<T> getRest();

    public abstract boolean isNil();

    public boolean equals(final ListNode<T> other) {
        ListNode<T> thisCur = this;
        ListNode<T> otherCur = other;

        while (!thisCur.isNil()) {
/*System.out.println("thisCur = " + thisCur);
System.out.println("    thisCur.getFirst() = " + thisCur.getFirst());
System.out.println("    otherCur = " + otherCur);
System.out.println("    otherCur.getFirst() = " + otherCur.getFirst());*/
            if (otherCur.isNil()) {
                return false;
            }

            if (!thisCur.getFirst().equals(otherCur.getFirst())) {
                return false;
            }

            thisCur = thisCur.getRest();
            otherCur = otherCur.getRest();
        }

        return otherCur.isNil();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("(");

        ListNode<T> cur = this;
        while (!cur.isNil()) {
            sb.append(cur.getFirst());

            cur = cur.getRest();
            if (!cur.isNil()) {
                sb.append(", ");
            }
        }

        sb.append(")");
        return sb.toString();
    }
}

