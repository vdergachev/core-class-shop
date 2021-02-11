/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package com.coreclass.shop;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.join;
import static java.util.Comparator.comparing;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class ShopApplication {

    public static void main(String[] args) {
        var shop = new Shop(10, 13, 15, 17);
        var numbers = shop.serve(args);
        System.out.println(join(" ", numbers));
    }

    private static class Kassa {

        private static final int MAX_QUEUE_CAPACITY = 20;

        private final int velocity;
        private int queuedNumber;

        public Kassa(int velocity) {
            this.velocity = velocity;
        }

        public int leave() {
            if (queuedNumber == 0) {
                throw new IllegalStateException("Kassa's queue already empty");
            }
            return queuedNumber--;
        }

        public int queue() {
            if (queuedNumber == MAX_QUEUE_CAPACITY) {
                throw new IllegalStateException("Kassa's queue is full");
            }
            return queuedNumber++;
        }

        public boolean isAvailable() {
            return queuedNumber < MAX_QUEUE_CAPACITY;
        }

        public BigDecimal getWaitingTime() {
            if (queuedNumber == 0) {
                return BigDecimal.ZERO; // Если очередь кассы пуста - время ожидания в ней = 0
            }
            return BigDecimal.valueOf(queuedNumber).divide(BigDecimal.valueOf(velocity), 4, RoundingMode.HALF_EVEN);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Kassa kassa = (Kassa) o;
            return velocity == kassa.velocity;
        }

        @Override
        public int hashCode() {
            return Objects.hash(velocity);
        }
    }

    private static class Shop {

        // Как альтернатива - добавить кассе атрибут - порядковый номер и использовать List вместо Map
        private final Map<Integer, Kassa> kassas;

        // Так же можно сделать ShopBuilder, можно обернуть массив в конфигурацию
        public Shop(final int... kassirVelocity) {
            var index = new AtomicInteger(0);
            kassas = Arrays.stream(kassirVelocity)
                    .mapToObj(Kassa::new)
                    .collect(toMap(x -> index.incrementAndGet(), identity()));
        }

        public List<String> serve(final String[] customerEvents) {
            return Arrays.stream(customerEvents)
                    .map(this::handleCustomerEvent)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(toList());
        }

        private Optional<String> handleCustomerEvent(final String customerEvent) {
            if (customerEvent.equalsIgnoreCase("A")) {
                return Optional.of(resolveAvailableKassa());
            } else {
                leaveKassa(Integer.parseInt(customerEvent));
                return Optional.empty();
            }
        }

        private void leaveKassa(int index) {
            kassas.get(index).leave();
        }

        private String resolveAvailableKassa() {
            // получаем саммую доступную кассу (по времени обслуживания и порядковому номеру)
            var kassaEntry = kassas.entrySet()
                    .stream()
                    .filter(entry -> entry.getValue().isAvailable())
                    .min(comparing((Map.Entry<Integer, Kassa> entry) -> entry.getValue().getWaitingTime())
                            .thenComparingInt(Map.Entry::getKey))
                    .get();

            // встаем в ее очредь
            kassaEntry.getValue().queue();
            // возвращаем порядковый номер кассы
            return kassaEntry.getKey().toString();
        }

    }
}
