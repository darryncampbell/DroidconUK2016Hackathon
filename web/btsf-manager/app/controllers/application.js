import Player from "../models/player";
import ShotEvent from "../models/shotEvent";

export default Ember.Controller.extend({

  init: function () {
    var config = {
      apiKey: 'AIzaSyA6JlIw9KuU9XWXHb-qLsDwZH-dzjyl42U',
      authDomain: 'bstf-d63cc.firebaseapp.com',
      databaseURL: 'https://bstf-d63cc.firebaseio.com',
      storageBucket: 'bstf-d63cc.appspot.com',
    };

    let self = this;

    let database = firebase.initializeApp(config).database();
    self.set('database', database);
    this.set('gameSessionId', 1);
  },

  showGameSession: function (id) {
    let self = this;
    let databaseReference = this.get('database').ref('game_session_' + id);
    this.set('databaseReference', databaseReference);

    databaseReference.on('value', function (snapshot) {
      let snapshotValue = snapshot.val();
      console.log(snapshotValue);

      let shotsFired = Ember.A();

      if (!snapshotValue.shotsFired) {
        snapshotValue.shotsFired = [];
      }

      for (let i = 0; i < snapshotValue.shotsFired.length; i++) {
        if (!snapshotValue.shotsFired[i]) {
          continue;
        }

        shotsFired.pushObject(ShotEvent.create(snapshotValue.shotsFired[i]));
      }

      shotsFired = self.sortAndFilterEvents(shotsFired);

      let players = Ember.A();

      if (!snapshotValue.players) {
        snapshotValue.players = [];
      }

      for (let i = 0; i < snapshotValue.players.length; i++) {
        if (!snapshotValue.players[i]) {
          continue;
        }

        let player = Player.create(snapshotValue.players[i]);
        player.set('shotsFired', shotsFired);
        players.pushObject(player);
      }

      self.set('model', Ember.Object.create(snapshotValue));
      self.set('model.players', players);
      self.set('model.shotsFired', shotsFired);
    });
  },

  sortAndFilterEvents: function (shotsFired) {
    if (shotsFired.length == 0) return shotsFired;
    let sortedByTime = shotsFired.sortBy('millis');
    let count = shotsFired.length > 10 ? 10 : shotsFired.length;

    if (count === 1) {
      return shotsFired;
    }

    let result = Ember.A();

    for (let index = count - 1; index != 0; index--) {
      result.pushObject(sortedByTime[index]);
    }

    return result;
  },

  actions: {
    openSession: function () {
      this.showGameSession(this.get('gameSessionId'));
    },

    stopSession: function () {
      let gameSession = {
        id: this.get('model.id'),
        players: [],
        started: false
      };

      this.get('databaseReference').set(gameSession);
    }
  }
})
