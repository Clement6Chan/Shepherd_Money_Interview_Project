package com.shepherdmoney.interviewproject.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class CreditCard {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private String issuanceBank;

    private String number;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // Credit card's owner



    @OneToMany(mappedBy = "creditCard", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BalanceHistory> balanceHistory = new ArrayList<>();

    public void update_balance(Instant entered_date, double charge){
        //System.out.println(balanceHistory);
        //part one: update existing balances
        //System.out.println("Part one:");
        for (BalanceHistory balance_entry: balanceHistory){
            //System.out.println(balance_entry);
            if (!balance_entry.getDate().isBefore(entered_date)){
                balance_entry.update_balance(charge);
            }
        }

        //part two: insert missing balances before existing ones
        //System.out.println("Part two");
        Instant today = LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant latest = entered_date;
        Instant earliest = entered_date.plus(-1,ChronoUnit.DAYS);
        double balance = 0.0;

        if (balanceHistory.isEmpty()){
            balance = charge;
        }
        else{
            balance = balanceHistory.get(0).getBalance();
            //cursors:
            latest = balanceHistory.get(0).getDate().plus(1,ChronoUnit.DAYS);
            earliest = balanceHistory.get(balanceHistory.size()-1).getDate().plus(-1,ChronoUnit.DAYS);
        }

        while (!latest.isAfter(today)){
            //System.out.println(latest);
            BalanceHistory new_BH_entry = new BalanceHistory();
            new_BH_entry.setBalance(balance);
            new_BH_entry.setDate(latest);
            new_BH_entry.setCreditCard(this);
            //System.out.println(new_BH_entry);
            balanceHistory.add(0,new_BH_entry); //add to front
            latest = latest.plus(1, ChronoUnit.DAYS);
        }

        // Part 3: insert missing balances after existing ones
        //System.out.println("part three: ");
        while (!earliest.isBefore(entered_date)){
            //System.out.println(earliest);
            BalanceHistory new_BH_entry = new BalanceHistory();
            new_BH_entry.setBalance(charge);
            new_BH_entry.setDate(earliest);
            new_BH_entry.setCreditCard(this);
            balanceHistory.add(balanceHistory.size(),new_BH_entry); //add to back
            //System.out.println(new_BH_entry);
            earliest = earliest.plus(-1, ChronoUnit.DAYS);
        }

    }



    @Override
    public String toString() {
        return "CreditCard{" +
                "id=" + id +
                ", issuanceBank='" + issuanceBank + '\'' +
                ", number='" + number + '\'' +
                '}';
    }
}
