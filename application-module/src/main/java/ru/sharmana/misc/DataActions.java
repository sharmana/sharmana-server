package ru.sharmana.misc;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableCollection;
import ru.sharmana.beans.Transaction;

import java.util.List;

import static com.google.common.collect.FluentIterable.from;
import static jersey.repackaged.com.google.common.collect.Lists.newArrayList;

/**
 * User: lanwen
 * Date: 11.10.14
 * Time: 23:44
 */
public class DataActions {

    public static List<Transaction> mergeTransactions(List<Transaction> original, List<Transaction> actual) {
        ImmutableCollection<Transaction> iterable = from(original).append(actual)
                .index(new Function<Transaction, Integer>() {
                    @Override
                    public Integer apply(Transaction input) {
                        return input.hashCode();
                    }
                }).values();
        return newArrayList(iterable);
    }
}
