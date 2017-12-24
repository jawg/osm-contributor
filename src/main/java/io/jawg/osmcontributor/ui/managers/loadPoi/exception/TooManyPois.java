package io.jawg.osmcontributor.ui.managers.loadPoi.exception;

public class TooManyPois extends RuntimeException {
   private Long count;

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}
