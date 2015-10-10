


---

# Requirements #
Be sure to have:
  * FreeMind 0.9.0 Beta version
  * Two open ports on your firewall (and on every collaborator's firewall)


# Download #

Current beta version of SharedMind can be downloaded from:
  * Windows installer [download](http://sharedmind.googlecode.com/files/SharedMind-b2_win-installer.exe).
  * Archive file [download](http://sharedmind.googlecode.com/files/SharedMind-b2.zip).

# Getting Started #

## Starting Collaboration Plug-in ##

  1. Run FreeMind.
  1. Save the active map. SharedMind does not allow unnamed map.
  1. Go to Tools > Start Collaboration Mode or press Alt+B. ![http://sharedmind.googlecode.com/files/startCollaborationScreenshot.png](http://sharedmind.googlecode.com/files/startCollaborationScreenshot.png)
  1. Fill in user id and port to be used then press Login. SharedMind uses two ports: the port number to be used is the one entered in the login window and the next port number (in the picture, the port number 1111 and 1112 will be used).
**Remember to open these two ports on your firewall.**

![http://sharedmind.googlecode.com/files/loginWindowScreenshoot.png](http://sharedmind.googlecode.com/files/loginWindowScreenshoot.png)

## Sharing a Map ##

After login, sharing window will appear as shown below.

![http://sharedmind.googlecode.com/files/sharingWindowScreenshoot-2.png](http://sharedmind.googlecode.com/files/sharingWindowScreenshoot-2.png)

To start sharing a map:
  1. Press 'Connect' button.
  1. When a dialog appear asking whether you have the current map or not choose 'Yes'.

## Joining an Existing Group ##

To join an existing group:
  1. If you already have an older version of the shared map open it.
  1. Fill the text-box beside the 'Connect' button with the IP address (or the host name) and the port of a users who is currently sharing her map (example: '127.0.0.1:1111' or 'localhost:1111'); then press 'Connect'.
  1. Answer the dialog that whether you already have the previous version of the map or not.
    * If you choose 'No', the current version of the map will be loaded.
    * Otherwise, your local copy of the map will be merged with the current shared version. If there are no conflicting changes the merged map will be reloaded in all clients, otherwise the current shared map and another map used for [manual conflict resolution](UserGuide#Resolving_Offline_Conflict.md) will be displayed.

## Inviting Friend to Connect via Google Chat ##

To login to Google chat click 'Google Chat Login' button in the Sharing Window.

![http://sharedmind.googlecode.com/files/googleChatLoginScreenshot.png](http://sharedmind.googlecode.com/files/googleChatLoginScreenshot.png)

After logging in, to invite friend for sharing:
  1. Choose the name of friend you want to invite in the contact list window.
  1. Press 'Invite' button.
  1. Choose your correct ip address and port from the dialog that appear and click 'Ok'.

![http://sharedmind.googlecode.com/files/inviteDialogScreenshot.png](http://sharedmind.googlecode.com/files/inviteDialogScreenshot.png)

If the user that is invited is online, on SharedMind a dialog asking whether he/she wants to collaborate or not will be popped out and on other chat clients a message 'Hi i hope to collaborate with you on SharedMind' will be shown.


## Online Editing ##

When a user is online, the changes made by the user will be sent to all other online users in (almost) real-time.

When more than one user makes a change at the same time that is conflicting (example: 2 users change the text of the same node), the conflicting changes are rolled back in all users' versions. Then, the application provides a dialog for redoing the rolled back actions.

The list of conflicting actions will be shown to all users, but each user can only redo his/her own actions.

![http://sharedmind.googlecode.com/files/redoDialogScreenshoot.png](http://sharedmind.googlecode.com/files/redoDialogScreenshoot.png)

In the dialog shown above, the first table shows the user's actions that have been canceled and the second table shows all actions that have been canceled. A user can redo the actions shown on the first table.

## Resolving Offline Conflict ##

If you have the group's map, but you're disconnected from the group, you can still modify your local copy of the map. Upon reconnecting to the group, SharedMind will merge your local copy with the current group's version of the map.

If there are conflicting changes that cannot be merged automatically, SharedMind will try to merge the maps as much as possible and then provide an interface to resolve the remaining conflicting changes manually.

Interface for manual conflict resolution is shown below:
![http://sharedmind.googlecode.com/files/mergingMapScreenshoot.png](http://sharedmind.googlecode.com/files/mergingMapScreenshoot.png)

Here is the explanation of the window above:
  * The current (group's) version of the shared map is loaded (1) so, while resolving conflicts, you can still collaborate by modifying the group's version.
  * Map shown in the window is the map used for merging (2). Only one conflict is shown at a time. The map has 3 branches that show:
    * Shared map with non-conflicting modifications from the local map (4).
    * Local map with non-conflicting modifications from the shared map (5).
    * Merged map (6).
  * The merging dialog (3) provides buttons to go back and forth between conflicts and to mark each conflict as resolved.

For each conflict, the nodes that are actually conflicting are marked by a cross icon.

When the last conflict is marked as resolved, the merged map branch (6) will be accepted as the final map. This map is then merged automatically against the current (group's) shared map. If there are no new conflicts, the map will be loaded by all the online users. If new conflicts appeared, you'll use the same interface to resolve them.

We define what we consider as a conflict [here](ConflictDefinition.md).