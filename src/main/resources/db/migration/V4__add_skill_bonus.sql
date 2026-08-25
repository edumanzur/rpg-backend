alter table tb_races add column skill_bonus_name varchar(50);
alter table tb_races add column skill_bonus_value integer not null default 0;

alter table tb_classes add column skill_bonus_name varchar(50);
alter table tb_classes add column skill_bonus_value integer not null default 0;
