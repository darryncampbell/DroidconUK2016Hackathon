import Player from "../models/player";

export default Ember.Controller.extend({

  init: function () {
    var config = {
      apiKey: 'AIzaSyA6JlIw9KuU9XWXHb-qLsDwZH-dzjyl42U',
      authDomain: 'bstf-d63cc.firebaseapp.com',
      databaseURL: 'https://bstf-d63cc.firebaseio.com',
      storageBucket: 'bstf-d63cc.appspot.com',
    };

    let self = this;

    let databaseReference = firebase.initializeApp(config).database().ref('game_session_2');
    databaseReference.on('value', function (snapshot) {
      let snapshotValue = snapshot.val();
      console.log(snapshotValue);
      let players = Ember.A();

      for (let i = 0; i < snapshotValue.players.length; i++) {
        if (!snapshotValue.players[i]) {
          continue;
        }

        players.pushObject(Player.create(snapshotValue.players[i]));
      }

      self.set('model', Ember.Object.create(snapshotValue))
      self.set('model.players', players);
    });
  }
})
