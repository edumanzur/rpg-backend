-- Additive migration: notes become private per-author (including the
-- campaign master's own — this is now a personal-journal feature, not a
-- shared GM handout tool). Any pre-existing note (created back when notes
-- were campaign-wide) has no author and becomes invisible to everyone going
-- forward, since reads now filter by author_id = current user — acceptable,
-- there is no way to attribute historical authorship retroactively.

alter table tb_notes add column author_id bigint;
alter table tb_notes add constraint fk_notes_author foreign key (author_id) references tb_users;
