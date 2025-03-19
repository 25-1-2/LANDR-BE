package com.landr.domain.lecture;


import jakarta.persistence.*;

@Entity
@Table(name = "lessons", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"lecture_id", "sequence"})
})
public class Lesson {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Lecture lecture;

    @Column(nullable = false)
    private int sequence;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private int duration;
}
