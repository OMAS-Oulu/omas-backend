package com.omas.webapp.table;

import java.sql.Date;
import java.time.Instant;

import jakarta.persistence.Entity; 
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor; 

@Entity
@Data
@AllArgsConstructor
@Table
@NoArgsConstructor
public class Club { 

	@Id
	private String name; 
	
	private String nameNonId;
	private Date creationDate;
	private long idCreator;

	public Club(String clubName, String clubId, long creatorId) {
		this.name = clubId;
		this.nameNonId = clubName;
		this.idCreator = creatorId;
		this.creationDate = new Date(Instant.now().toEpochMilli());
	}
} 