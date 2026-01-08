package com.picpick.mart;

import com.picpick.martItem.MartItem;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigInteger;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "marts")
public class Mart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "address")
    private String address;

    @Column(name = "registration_number", unique = true, nullable = false)
    private BigInteger registrationNumber;

    @Column(name = "document_file")
    private String documentFile;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "latitude")
    private Double latitude;

    @OneToMany(mappedBy = "mart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MartItem> martItems;
}
