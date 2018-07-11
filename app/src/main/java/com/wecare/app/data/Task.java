package com.wecare.app.data;


import com.google.protobuf.InvalidProtocolBufferException;
import com.wecare.app.data.entity.AddressBookProtos;

/**
 * Created by chengzj on 2017/6/17.
 */

public class Task {
    public static void main(String[] args){
        AddressBookProtos.Person.PhoneNumber phoneNumber = AddressBookProtos.Person.PhoneNumber.newBuilder()
                .setNumber("18202745852")
                .setType(AddressBookProtos.Person.PhoneType.HOME)
                .build();
        AddressBookProtos.Person person = AddressBookProtos.Person.newBuilder()
                .setId(1)
                .setName("text")
                .setEmail("123")
                .addPhone(phoneNumber)
                .build();
        try {
            long start = System.currentTimeMillis();
            //编码
            byte[] bytes = person.toByteArray();
            //解码测试
            AddressBookProtos.Person p = AddressBookProtos.Person.parseFrom(bytes);
            System.out.println("================== start =================");
            System.out.println(p.getEmail());
            System.out.println(p.getPhone(0).getNumber());
            System.out.println("\n=========== PhoneNumber ============");
            System.out.println(phoneNumber);
            System.out.println("===========  Person ===========");
            System.out.println(p);


            System.out.println("excute time is：" + (System.currentTimeMillis() - start));
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }
}
