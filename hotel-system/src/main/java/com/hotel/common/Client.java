package com.hotel.common;

import java.io.Serializable;

public class Client implements Serializable {
    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone; // <--- 1. Добавляем поле

    // 2. Обновляем конструктор
    public Client(int id, String firstName, String lastName, String email, String phone) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone; // <--- Сохраняем
    }

    public int getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    // Для удобного отображения полного имени, если понадобится
    public String getFullName() {
        return firstName + " " + lastName;
    }
}