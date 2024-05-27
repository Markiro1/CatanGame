DELETE FROM user_to_lobby WHERE EXISTS(select * from user_to_lobby);
DELETE FROM lobbies WHERE EXISTS(select * from lobbies);