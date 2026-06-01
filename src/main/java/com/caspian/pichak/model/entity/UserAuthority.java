package com.caspian.pichak.model.entity;


import jakarta.persistence.*;

@Entity
@Table(name = "user_authority",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "authority_id"},
                name = "USER_AUTHORITY_UNIQUE_USER_ID_AND_AUTHORITY_ID")})
public class UserAuthority {
    @Id
    @Column(name = "ID", unique = true, nullable = false, columnDefinition = "NUMBER(10)")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne
    @JoinColumn(name = "USER_ID", foreignKey = @ForeignKey(name = "FK_USER_AUTHORITY_USER_ID"))
    private Users users;
    @ManyToOne
    @JoinColumn(name = "AUTHORITY_ID", foreignKey = @ForeignKey(name = "FK_USER_AUTHORITY_AUTHORITY_ID"))
    private Authority authority;

    public Integer getId() {
        return this.id;
    }

    public Users getUsers() {
        return this.users;
    }

    public Authority getAuthority() {
        return this.authority;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public void setUsers(final Users users) {
        this.users = users;
    }

    public void setAuthority(final Authority authority) {
        this.authority = authority;
    }
}
