CREATE TABLE policy(
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  holder_party TEXT NOT NULL,
  insured_party TEXT NOT NULL,
  beneficiary_party TEXT NOT NULL,
  from_date DATE NOT NULL,
  thru_date DATE NOT NULL,
  main_sum_insured DECIMAL(10,2) NOT NULL,
  hsdr17_sum_insured DECIMAL(10,2),
  ccb17_sum_insured DECIMAL(10,2),
  ccbh17_sum_insured DECIMAL(10,2),
  premium DECIMAL(10,2) NOT NULL
);
