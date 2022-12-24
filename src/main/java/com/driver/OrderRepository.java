package com.driver;

import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class OrderRepository {
    Map<String,Order> orderMap=new HashMap<>();
    Map<String,DeliveryPartner> partnerMap=new HashMap<>();
    Map<String, HashSet<String>> partnerOrdersMap=new HashMap<>();
    Map<String,String> orderTimeMap=new HashMap<>();
    public void addOrder(Order order) {
        orderMap.put(order.getId(),order);
    }

    public void addPartner(String partnerId) {
        DeliveryPartner partner=new DeliveryPartner(partnerId);
        partnerMap.put(partner.getId(),partner);
    }

    public void createOrderPartnerPair(String orderId, String partnerId) {
        if(orderMap.containsKey(orderId) && partnerMap.containsKey(partnerId)){

            HashSet<String> orderIDSet;
            if(partnerOrdersMap.containsKey(partnerId)) orderIDSet=partnerOrdersMap.get(partnerId);
            else orderIDSet=new HashSet<>();
            orderIDSet.add(orderId);
            partnerOrdersMap.put(partnerId,orderIDSet);

            DeliveryPartner partner=partnerMap.get(partnerId);
            partner.setNumberOfOrders(orderIDSet.size());


            orderTimeMap.put(orderId,partnerId);
        }
    }

    public Order getOrderById(String orderId) {
        return orderMap.get(orderId);
    }

    public DeliveryPartner getPartnerById(String partnerId) {
        return partnerMap.get(partnerId);
    }

    public Integer getOrderCountByPartnerId(String partnerId) {
        Integer count=0;
        if(partnerMap.containsKey(partnerId)) count=partnerMap.get(partnerId).getNumberOfOrders();
        return count;
    }

    public List<String> getOrdersByPartnerId(String partnerId) {
        HashSet<String> orderList=new HashSet<>();
        if(partnerOrdersMap.containsKey(partnerId)) orderList=partnerOrdersMap.get(partnerId);
        return new ArrayList<>(orderList);
    }

    public List<String> getAllOrders() {
        return new ArrayList<>(orderMap.keySet());
    }

    public Integer getCountOfUnassignedOrders() {
        Integer count=0;
        List<String> orders=new ArrayList<>(orderMap.keySet());
        for(String OrderID:orders){
            if(!orderTimeMap.containsKey(OrderID)) count +=1;
        }
        return count;
    }

    public Integer getOrdersLeftAfterGivenTimeByPartnerId(String time, String partnerId) {
        Integer count=0;
        Integer userTime=Integer.valueOf(time.substring(0,2))*60+Integer.valueOf(time.substring(3));
        if(partnerOrdersMap.containsKey(partnerId)) {
            for (String orderId : partnerOrdersMap.get(partnerId)) {
                if(orderMap.containsKey(orderId)) {
                    if (orderMap.get(orderId).getDeliveryTime() > userTime) count +=1;
                }
            }
        }
        return count;
    }

    public String getLastDeliveryTimeByPartnerId(String partnerId) {
        Integer time=0;
        if(partnerOrdersMap.containsKey(partnerId)) {
            for (String orderId : partnerOrdersMap.get(partnerId)) {
                if(orderMap.containsKey(orderId)) time = Math.max(time, orderMap.get(orderId).getDeliveryTime());
            }
        }
        // convert time to string
        String hours=String.valueOf(time/60);
        String minutes=String.valueOf(time%60);

        if(hours.length()==1) hours="0"+hours;
        if(minutes.length()==1) minutes="0"+minutes;
        return hours+":"+minutes;
    }

    public void deletePartnerById(String partnerId) {

        if(partnerOrdersMap.containsKey(partnerId)) {
            for (String orderID : partnerOrdersMap.get(partnerId)) {
                if(orderTimeMap.containsKey(orderID)) orderTimeMap.remove(orderID);
            }
            partnerOrdersMap.remove(partnerId);
        }

        if(partnerMap.containsKey(partnerId)) partnerMap.remove(partnerId);
    }

    public void deleteOrderById(String orderId) {

        if(orderTimeMap.containsKey(orderId)){
            String partnerId=orderTimeMap.get(orderId);
            HashSet<String> orders=partnerOrdersMap.get(partnerId);
            orders.remove(orderId);
            partnerOrdersMap.put(partnerId,orders);

            DeliveryPartner partner=partnerMap.get(partnerId);
            partner.setNumberOfOrders(orders.size());
            partnerMap.put(partnerId,partner);

            orderTimeMap.remove(orderId);
        }

        if(orderMap.containsKey(orderId)) orderMap.remove(orderId);
    }
}
