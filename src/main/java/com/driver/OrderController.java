package com.driver;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("orders")
public class OrderController {

    HashMap<String,Order> orderMap=new HashMap<>();
    HashMap<String, DeliveryPartner>partnerMap=new HashMap<>();
    HashMap<String ,List<String>>partnerOrderMap=new HashMap<>();
    HashMap<String,Integer>orderTimeMap=new HashMap<>();
    @PostMapping("/add-order")
    public ResponseEntity<String> addOrder(@RequestBody Order order){
            orderMap.put(order.getId(),order);
            List<Integer>time=new ArrayList<>();
            time.add(order.getDeliveryTime());
        Collections.sort(time);
        orderTimeMap.put(order.getId(),order.getDeliveryTime());
        return new ResponseEntity<>("New order added successfully", HttpStatus.CREATED);
    }

    @PostMapping("/add-partner/{partnerId}")
    public ResponseEntity<String> addPartner(@PathVariable String partnerId){
        DeliveryPartner partner=new DeliveryPartner(partnerId);
        partnerMap.put(partnerId,partner);
        return new ResponseEntity<>("New delivery partner added successfully", HttpStatus.CREATED);
    }

    @PutMapping("/add-order-partner-pair")
    public ResponseEntity<String> addOrderPartnerPair(@RequestParam String orderId, @RequestParam String partnerId){

        //This is basically assigning that order to that partnerId
        if (partnerOrderMap.containsKey(partnerId)){
            partnerOrderMap.get(partnerId).add(orderId);
        }else{
            List<String> orders=new ArrayList<>();
            orders.add(orderId);
            partnerOrderMap.put(partnerId,orders);
        }
        return new ResponseEntity<>("New order-partner pair added successfully", HttpStatus.CREATED);
    }

    @GetMapping("/get-order-by-id/{orderId}")
    public ResponseEntity<Order> getOrderById(@PathVariable String orderId){

        Order order= null;
        if (orderMap.containsKey(orderId)){
            order=orderMap.get(orderId);
        }
        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }

    @GetMapping("/get-partner-by-id/{partnerId}")
    public ResponseEntity<DeliveryPartner> getPartnerById(@PathVariable String partnerId){

        DeliveryPartner deliveryPartner = null;
        if (partnerMap.containsKey(partnerId)){
            deliveryPartner=partnerMap.get(partnerId);
        }
        //deliveryPartner should contain the value given by partnerId
        return new ResponseEntity<>(deliveryPartner, HttpStatus.CREATED);
    }

    @GetMapping("/get-order-count-by-partner-id/{partnerId}")
    public ResponseEntity<Integer> getOrderCountByPartnerId(@PathVariable String partnerId){

        Integer orderCount = 0;
        if (partnerOrderMap.containsKey(partnerId)){
            orderCount=partnerOrderMap.get(partnerId).size();
        }
        //orderCount should denote the orders given by a partner-id
        return new ResponseEntity<>(orderCount, HttpStatus.CREATED);
    }

    @GetMapping("/get-orders-by-partner-id/{partnerId}")
    public ResponseEntity<List<String>> getOrdersByPartnerId(@PathVariable String partnerId){
        List<String> orders = null;
        if (partnerOrderMap.containsKey(partnerId)){
            orders=partnerOrderMap.get(partnerId);
        }
        //orders should contain a list of orders by PartnerId
        return new ResponseEntity<>(orders, HttpStatus.CREATED);
    }

    @GetMapping("/get-all-orders")
    public ResponseEntity<List<String>> getAllOrders(){
        List<String> orders = null;
        for (String key:orderMap.keySet()) {
            orders.add(key);
        }
        //Get all orders
        return new ResponseEntity<>(orders, HttpStatus.CREATED);
    }

    @GetMapping("/get-count-of-unassigned-orders")
    public ResponseEntity<Integer> getCountOfUnassignedOrders(){
        Integer countOfOrders = 0;
        HashSet<String>orderSet=new HashSet<>();
        for (String key:partnerOrderMap.keySet()) {
            List<String >orderId=partnerOrderMap.get(key);
            for (String str:orderId){
                orderSet.add(str);
            }
        }
        for (String str:orderMap.keySet()){
            if (orderSet.contains(str)){
                countOfOrders++;
            }
        }
        //Count of orders that have not been assigned to any DeliveryPartner

        return new ResponseEntity<>(countOfOrders, HttpStatus.CREATED);
    }

    @GetMapping("/get-count-of-orders-left-after-given-time/{partnerId}")
    public ResponseEntity<Integer> getOrdersLeftAfterGivenTimeByPartnerId(@PathVariable String time, @PathVariable String partnerId){

        Integer countOfOrders = 0;
        int a=Integer.valueOf(time.substring(0,3));
        int b=Integer.valueOf(time.substring(4,time.length()));
        int t=(a*60)+b;
        List<String> list=partnerOrderMap.get(partnerId);
        for(String str:list){
            if (t<orderTimeMap.get(str)){
                countOfOrders++;
            }
        }
        //countOfOrders that are left after a particular time of a DeliveryPartner
        return new ResponseEntity<>(countOfOrders, HttpStatus.CREATED);
    }

    @GetMapping("/get-last-delivery-time/{partnerId}")
    public ResponseEntity<String> getLastDeliveryTimeByPartnerId(@PathVariable String partnerId){
        String time = null;
        int maxTime=0;
        List<String>list=partnerOrderMap.get(partnerId);
        for (String str:list) {
            if (maxTime<orderTimeMap.get(str)){
                maxTime=orderTimeMap.get(str);
            }
        }
        int h=maxTime/60;
        int m=maxTime%60;
        time= Integer.toString(h)+Integer.toString(m);
        //Return the time when that partnerId will deliver his last delivery order.
        return new ResponseEntity<>(time, HttpStatus.CREATED);
    }

    @DeleteMapping("/delete-partner-by-id/{partnerId}")
    public ResponseEntity<String> deletePartnerById(@PathVariable String partnerId){
        //Delete the partnerId
        //And push all his assigned orders to unassigned orders.
        partnerMap.remove(partnerId);
        partnerOrderMap.remove(partnerId);
        return new ResponseEntity<>(partnerId + " removed successfully", HttpStatus.CREATED);
    }

    @DeleteMapping("/delete-order-by-id/{orderId}")
    public ResponseEntity<String> deleteOrderById(@PathVariable String orderId){

        //Delete an order and also
        // remove it from the assigned order of that partnerId
        orderMap.remove(orderId);
        for (String key:partnerOrderMap.keySet()) {
            List<String>list=partnerOrderMap.get(key);
            for (String str:list){
                if (str.equals(orderId)){
                    list.remove(orderId);
                    partnerOrderMap.remove(key);
                    partnerOrderMap.put(key,list);
                }
            }
        }

        return new ResponseEntity<>(orderId + " removed successfully", HttpStatus.CREATED);
    }
}
