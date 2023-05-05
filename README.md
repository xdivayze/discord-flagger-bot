# discord-flagger-bot

<h1>Simple Discord Flagged Word Blocker Bot Using JDA </h1>
This bot utilises JDA to delete and log the word and distance to which word it was blocked due to.
<br>
It utilises Levenshtein distance algorithm to calculate distance between the sent message <br>
and query it through each of the words in flagged word list and if a word is selected where the distance <br>
is smaller than 1.5 the message is deleted and a log is sent as a message and saved to cache.txt
