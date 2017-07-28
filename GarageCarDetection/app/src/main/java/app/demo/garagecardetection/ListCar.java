/*
 * Copyright (c) 2015.
 * Author: Son Bui
 */

package app.demo.garagecardetection;

import java.io.Serializable;
import java.util.List;
import java.util.Vector;

public class ListCar implements Serializable {

    private static final long serialVersionUID = 1L;
    private int itemcount = 0;

    private List<CarItem> itemlist;

    public List<CarItem> getItemlist() {
        return itemlist;
    }

    public ListCar() {
        itemlist = new Vector<CarItem>(0);
    }

    public void addItem(CarItem item) {
        itemlist.add(item);
        itemcount++;
    }

    public void addItem(int position, CarItem item) {//Dam bao cung 1 luc 2 cai cùng load se ko add lan cua nhau
        itemlist.add(position, item);
        itemcount++;
    }

    public void setItem(int position, CarItem item) {
        itemlist.set(position, item);
    }

    public void removeItem(CarItem item) {
        itemlist.remove(item);
        itemcount--;
    }

    public void removeItem(int position) {
        itemlist.remove(position);
        itemcount--;
    }

    public CarItem getItem(int location) {
        return itemlist.get(location);
    }


    public int getItemCount() {
        return itemcount;
    }

    public void clear() {
        itemlist.clear();
        itemcount = 0;
    }

    public void addAllItem(List<CarItem> listTypeGroups) {
        itemlist.addAll(listTypeGroups);
        itemcount = itemcount + itemlist.size();
    }


}
