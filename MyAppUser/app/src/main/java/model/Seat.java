package model;

public class Seat {
    private boolean isBooked;
    private boolean isSelected;
    private int row;
    private int column;

    // Constructor to initialize the seat with row, column, and booked status
    public Seat(int row, int column, boolean isBooked) {
        this.row = row;
        this.column = column;
        this.isBooked = isBooked;
        this.isSelected = false;  // Initially, the seat is not selected
    }

    // Getters for row and column
    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public boolean isBooked() {
        return isBooked;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
