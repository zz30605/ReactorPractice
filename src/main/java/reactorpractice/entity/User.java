package reactorpractice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document
public class User {
	@Id
	@JsonIgnore
	private String id;      // 注解属性id为ID
	@Indexed(unique = true)       // 注解属性username为索引，并且不能重复
	private String username;
	private String name;
	private String phone;
	private Date birthday;
}
