SELECT a.CLIENT_ID
	, a.AGE
	, a.MARITAL_STATUS
	, b.C_PROD, b.AGG_OUSTANDING
	, c.C_TXN, c.AGG_TXN
	FROM party as a
		LEFT JOIN (SELECT CLIENT_ID, COUNT(PRODUCT_ID) AS C_PROD, SUM(OUTSTANDING) AS AGG_OUSTANDING FROM products GROUP BY CLIENT_ID ORDER BY CLIENT_ID) as b
			ON a.CLIENT_ID = b.CLIENT_ID
		LEFT JOIN (SELECT CLIENT_ID, COUNT(TRANSACTION_ID) AS C_TXN, SUM(TRANSACTION_AMOUNT) AS AGG_TXN FROM transactions GROUP BY CLIENT_ID ORDER BY CLIENT_ID) as c
			ON a.CLIENT_ID = c.CLIENT_ID