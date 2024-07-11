CREATE TABLE selected_risk(
  policy_id UUID NOT NULL REFERENCES policy (id),
  risk_code TEXT NOT NULL,
  sum_insured DECIMAL(10,2) NOT NULL,
  seq_order INTEGER NOT NULL,

  PRIMARY KEY (policy_id, risk_code)
);

INSERT INTO selected_risk (SELECT policy.id, 'MAIN', policy.main_sum_insured FROM policy);
INSERT INTO selected_risk (SELECT policy.id, 'HSDR_17', policy.hsdr17_sum_insured FROM policy WHERE policy.hsdr17_sum_insured IS NOT NULL );
INSERT INTO selected_risk (SELECT policy.id, 'CCB_17', policy.ccb17_sum_insured FROM policy WHERE policy.ccb17_sum_insured IS NOT NULL );
INSERT INTO selected_risk (SELECT policy.id, 'CCBH_17', policy.ccbh17_sum_insured FROM policy WHERE policy.ccbh17_sum_insured IS NOT NULL );

ALTER TABLE policy DROP COLUMN main_sum_insured;
ALTER TABLE policy DROP COLUMN hsdr17_sum_insured;
ALTER TABLE policy DROP COLUMN ccb17_sum_insured;
ALTER TABLE policy DROP COLUMN ccbh17_sum_insured;
