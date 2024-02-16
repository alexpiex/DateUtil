# DateUtil
Libreia per la gestione delle date.

## Esempio di utilizzo:

```java
String dataOracle = "2015-02-28 11:13:11.0";
SimpleDateFormat sdf = new SimpleDateFormat();
sdf.applyPattern("yyyy-MM-dd HH:mm:sss");
Date date = sdf.parse(dataOracle);
System.out.println("Object Date: "+date);

DateUtil.setDatePattern("dd/MM/yyyy HH:mm:ss");

System.out.println("Current String: "+ DateUtil.currentString());
System.out.println("Current Date: "+ DateUtil.todayString());
System.out.println("Data Ieri: "+DateUtil.yesterday());
```

## License

[MIT](https://choosealicense.com/licenses/mit/)
