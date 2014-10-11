package ru.sharmana.misc;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableListMultimap;
import ru.sharmana.beans.Transaction;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.FluentIterable.from;

/**
 * User: lanwen
 * Date: 11.10.14
 * Time: 23:44
 */
public class DataActions {

    public static List<Transaction> mergeTransactions(List<Transaction> original, List<Transaction> actual) {
        ImmutableListMultimap<String, Transaction> index = from(original).append(actual)
                .index(new Function<Transaction, String>() {

                    @Override
                    public String apply(Transaction input) {
                        String join = Joiner.on("").join(
                                input.getComment(),
                                input.getWho(),
                                input.getCount(),
                                input.getDate());

                        return join;
                    }
                });
        List<Transaction> result = new ArrayList<>();
        for (String key : index.keySet()) {
            result.add(index.get(key).get(0));
        }
        return result;
    }
}
