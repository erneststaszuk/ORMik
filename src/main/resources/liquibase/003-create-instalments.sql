CREATE TABLE instalment(
  id UUID PRIMARY KEY,
  policy_id UUID,
  amount DECIMAL(10,2) NOT NULL,
  due DATE NOT NULL,
  is_paid BOOLEAN NOT NULL,
  version BIGINT NOT NULL
);
