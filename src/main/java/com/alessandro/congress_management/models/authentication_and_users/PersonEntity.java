package com.alessandro.congress_management.models.authentication_and_users;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "Person")
@Table(name= "person")
@Data
@NoArgsConstructor
public class PersonEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_person")
    private Long idPerson;

    @Column
    private String email;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column
    private String organization;

    @Column(name = "photo_url")
    private String photoUrl;

}
