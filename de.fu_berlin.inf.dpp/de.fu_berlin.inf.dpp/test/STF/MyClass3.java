package my.pkg;

import java.util.*;

public class MyClass3 {

        private static void fill(Collection c) {
                for (int i = 0; i < 5; i++)
                        c.add("" + i);
        }

        public static void main(String[] args) {
                Collection c = new LinkedList();
                fill(c);
                System.out.println(c); // [0, 1, 2, 3, 4]
        }
}