package brutes.server.net;

import brutes.server.db.DatasManager;
import brutes.server.db.entity.BonusEntity;
import brutes.server.db.entity.BruteEntity;
import brutes.server.db.entity.FightEntity;
import brutes.server.db.entity.NotFoundEntityException;
import brutes.server.db.entity.UserEntity;
import brutes.server.game.Bonus;
import brutes.server.game.Brute;
import brutes.server.game.Fight;
import brutes.server.game.User;
import brutes.net.Network;
import brutes.net.NetworkReader;
import brutes.net.Protocol;
import brutes.server.net.response.UserResponse;
import brutes.server.ui;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Karl
 * @author Thiktak
 */
public class NetworkLocalTestServer extends Network {

    public NetworkLocalTestServer(Socket connection) throws IOException {
        super(connection);
    }

    protected User checkTokenAndReturnUser(String token) throws IOException, SQLException, NetworkResponseException, NotFoundEntityException {
        User user = UserEntity.findByToken(token);
        if (user == null) {
            throw new NetworkResponseException(Protocol.ERROR_TOKEN);
        }
        return user;
    }

    synchronized public void read() throws IOException, SQLException {
        NetworkReader r = this.getReader();
        r.readMessageSize();
        byte disc = this.getReader().readDiscriminant();
        try {
            switch (disc) {
                case Protocol.D_CHEAT_FIGHT_LOOSE:
                    // token, newName
                    this.readCheatFightLoose(r.readString());
                    break;
                case Protocol.D_CHEAT_FIGHT_RANDOM:
                    // token, newName
                    this.readCheatFightRandom(r.readString());
                    break;
                case Protocol.D_CHEAT_FIGHT_WIN:
                    // token, newName
                    this.readCheatFightWin(r.readString());
                    break;
                case Protocol.D_CREATE_BRUTE:
                    // token, name
                    this.readCreateBrute(r.readString(), r.readString());
                    break;
                case Protocol.D_UPDATE_BRUTE:
                    // token, newName
                    this.readUpdateBrute(r.readString(), r.readString());
                    break;
                case Protocol.D_DELETE_BRUTE:
                    // token
                    this.readDeleteBrute(r.readString());
                    break;
                case Protocol.D_DO_FIGHT:
                    // token
                    this.readDoFight(r.readString());
                    break;
                case Protocol.D_GET_BONUS:
                    // token
                    this.readDataBonus(r.readLongInt());
                    break;
                case Protocol.D_GET_CHALLENGER_BRUTE_ID:
                    // id
                    this.readGetChallengerBruteId(r.readString());
                    break;
                case Protocol.D_GET_BRUTE:
                    // token
                    this.readDataBrute(r.readLongInt());
                    break;
                case Protocol.D_GET_MY_BRUTE_ID:
                    // id
                    this.readGetMyBruteId(r.readString());
                    break;
                case Protocol.D_LOGIN:
                    // pseudo, password
                    (new UserResponse(this.getWriter())).readLogin(r.readString(), r.readString());
                    break;
                case Protocol.D_LOGOUT:
                    // token
                    (new UserResponse(this.getWriter())).readLogout(r.readString());
                    break;
                case Protocol.D_GET_IMAGE:
                    // id
                    this.readGetImage(r.readLongInt());
                    break;
                /* @TODO define it !
                 * case Protocol.D_CREATE_USER:
                 *    // pseudo, password
                 *    this.readCreateUser(r.readString(), r.readString());
                 *    break;
                 * case Protocol.D_DELETE_USER:
                 *    // token
                 *    this.readDeleteUser(r.readString());
                 *    break;
                 */
                default:
                    throw new NetworkResponseException(Protocol.ERROR_SRLY_WTF);
            }
        } catch (NetworkResponseException e) {
            this.getWriter().writeDiscriminant(e.getError()).send();
        } catch (NotFoundEntityException e) {
            System.out.println("NotFoundEntityException " + e.getClassType() + " <- " + e.toString());
            if( e.getClassTypeIs(Bonus.class) ) {
                this.getWriter().writeDiscriminant(Protocol.ERROR_BONUS_NOT_FOUND).send();
            }
            else if( e.getClassTypeIs(Brute.class) ) {
                this.getWriter().writeDiscriminant(Protocol.ERROR_BRUTE_NOT_FOUND).send();
            }
            else if( e.getClassTypeIs(Fight.class) ) {
                this.getWriter().writeDiscriminant(Protocol.ERROR_FIGHT).send();
                /*@TODO define it ! ERROR_FIGHT_NOT_FOUND */
            }else if( e.getClassTypeIs(User.class) ) {
                this.getWriter().writeDiscriminant(Protocol.ERROR_TOKEN).send();
                /*@TODO define it ! ERROR_FIGHT_NOT_FOUND */
            }
            else {
                this.getWriter().writeDiscriminant(Protocol.ERROR_SRLY_WTF).send();
            }
        }
    }

    private void readCheatFightWin(String token) throws IOException, SQLException, NetworkResponseException, NotFoundEntityException {
        User user = this.checkTokenAndReturnUser(token);
        Fight fight = this.getFightWithChallenger(user);
        
        if( fight == null ) {
            throw new NetworkResponseException(Protocol.ERROR_FIGHT);
        }
        
        Brute brute = fight.getBrute1();
        
        // level UP !
        brute.setLevel((short)Math.min(brute.getLevel()+1, 100));
        
        // force UP !
        if( ui.random() ) // 50%
            switch(ui.random(1, 3)) {
                case 1: brute.setLife((short) (brute.getLife()+ui.random(1, 10))); break;
                case 2: brute.setSpeed((short) (brute.getSpeed()+ui.random(1, 5))); break;
                case 3: brute.setStrength((short) (brute.getStrength()+ui.random(1, 5))); break;
            }
        
        // Bonus UP
        if( ui.random() ) {
            Logger.getLogger(NetworkLocalTestServer.class.getName()).log(Level.INFO, "readCheatFightWin : bonus - random = yes");
            // S'il y a déjà 3 bonus
            Bonus bonusCharacter = BonusEntity.findRandomByBrute(brute);
            if( bonusCharacter != null ) {
                Logger.getLogger(NetworkLocalTestServer.class.getName()).log(Level.INFO, "readCheatFightWin : bonus - delete (" + bonusCharacter.getName() + ")");
                DatasManager.delete(bonusCharacter);
            }
            
            int AllCharacterBonus = ui.lengthObjects(BonusEntity.findAllByBrute(brute));

            Logger.getLogger(NetworkLocalTestServer.class.getName()).log(Level.INFO, "readCheatFightWin : bonus - length = " + AllCharacterBonus);
            if( AllCharacterBonus < 3 ) {
                // On trouve quelque chose ...
                Bonus bonusTreasure = BonusEntity.findRandom();
                bonusTreasure.setBruteId(brute.getId());
                
                bonusTreasure.setLevel((short) ui.randomMiddle(brute.getLevel()/2, .5));
                bonusTreasure.setStrength((short) ui.randomMiddle(brute.getStrength()/2, .5));
                bonusTreasure.setSpeed((short) ui.randomMiddle(brute.getSpeed()/2, .5));
                
                DatasManager.insert(bonusTreasure);
                Logger.getLogger(NetworkLocalTestServer.class.getName()).log(Level.INFO, "readCheatFightWin : bonus - insert (" + bonusTreasure.getName() + ")");
            }
        }
        
        DatasManager.save(brute);
        
        /*switch(ui.random(1, 6))
        {
            case 1: // Level Up
                brute.setLevel((short)(brute.getLevel()+1));
                Logger.getLogger(NetworkLocalTestServer.class.getName()).log(Level.INFO, "Result: +1 brute level ({0})", brute.getLevel());
                DatasManager.save(brute);
                break;
            case 2: // Bonus Up
            case 3: // Bonus Up
                Bonus bonus = BonusEntity.findRandomByBrute(brute);
                if( bonus != null )
                {
                    bonus.setLevel((short)(bonus.getLevel()+1));
                    bonus.setStrength((short)(((double)bonus.getStrength())*(1+Math.random())/2));
                    bonus.setSpeed((short)(((double)bonus.getSpeed())*(1+Math.random())/2));
                    DatasManager.save(bonus);
                    Logger.getLogger(NetworkLocalTestServer.class.getName()).log(Level.INFO, "Result: +1 bonus level ({0} [{1}])", new Object[]{bonus.getName(), bonus.getLevel()});
                }
                else
                {
                    bonus = BonusEntity.findRandomByBrute(fight.getBrute2());
                    if( bonus != null )
                    {
                        bonus.setLevel((short) ui.random(1, (int)(brute.getLevel()/2)));
                        bonus.setStrength((short)(((double)bonus.getStrength())*(1+Math.random())/2));
                        bonus.setSpeed((short)(((double)bonus.getSpeed())*(1+Math.random())/2));
                        bonus.setBruteId(brute.getId());
                        DatasManager.insert(bonus);
                        Logger.getLogger(NetworkLocalTestServer.class.getName()).log(Level.INFO, "Result: new bonus ({0} [{1}])", new Object[]{bonus.getName(), bonus.getLevel()});
                    }
                    
                }
                break;
            default: // New
                Logger.getLogger(NetworkLocalTestServer.class.getName()).log(Level.INFO, "Result: Nothing ...");
                break;
        }*/
        
        fight.setWinner(brute);
        DatasManager.save(fight);

        this.getWriter().writeDiscriminant(Protocol.R_FIGHT_RESULT)
                .writeBoolean(true)
                .send();
    }

    private void readCheatFightLoose(String token) throws IOException, SQLException, NetworkResponseException, NotFoundEntityException {
        User user = this.checkTokenAndReturnUser(token);
        Fight fight = this.getFightWithChallenger(user);
        
        if( fight == null ) {
            throw new NetworkResponseException(Protocol.ERROR_FIGHT);
        }

        fight.setWinner(fight.getBrute2());
        DatasManager.save(fight);

        this.getWriter().writeDiscriminant(Protocol.R_FIGHT_RESULT)
                .writeBoolean(false)
                .send();
    }

    private void readCheatFightRandom(String token) throws IOException, SQLException, NetworkResponseException, NotFoundEntityException {
        if (Math.random() < 0.5) {
            this.readCheatFightLoose(token);
        } else {
            this.readCheatFightWin(token);
        }
    }

    private void readDoFight(String token) throws IOException, SQLException, NetworkResponseException, NotFoundEntityException {
        User user = this.checkTokenAndReturnUser(token);
        Fight fight = this.getFightWithChallenger(user);
        
        int i = 0;
        int lost;
        while( fight.getBrute1().getLife() > 0 && fight.getBrute2().getLife() > 0 ) {
            
            for( int j = 0 ; j < 2 ; j++ )
            {
                Brute ch1 = j==0 ? fight.getBrute1() : fight.getBrute2();
                Brute ch2 = j==0 ? fight.getBrute2() : fight.getBrute1();
                int random = ui.random(0, 10);
                if( random == 0 ) {
                }
                else if( random == 1 ) {
                    ch1.setSpeed((short) (ch1.getSpeed()+1));
                    ch1.setStrength((short) (ch1.getStrength()+1));
                }
                else {
                    double pWin = ((double)(10*ch1.getLevel() + ch1.getStrength()));
                    pWin *= ((double) ch1.getSpeed()/((double)(1+ch1.getSpeed()+ch2.getSpeed())));
                    pWin *= ((double) ch1.getStrength()/((double)(1+ch1.getStrength()+ch2.getStrength())));
                    pWin *= 1+((double) ch1.getLife()/((double)(1+ch1.getLife()+ch2.getLife())));
                    // DEBUG
                    //System.out.println("@@" + pWin);
                    //System.out.println("@@ 100*(10*" + ch1.getLevel() + "+" + ch1.getStrength() + ")*(" + ch1.getSpeed() + "/(1+" + ch1.getSpeed() + "+" + ch2.getSpeed() + ")");
                    //System.out.println(ch1.getLife() + "/(1+" + ch1.getLife() + "+" + ch2.getLife() + ")");
                    ch2.setLife((short) (ch2.getLife() - pWin));
                }
            }
        }
        if( fight.getBrute1().getLife() > 0 ) {
            this.readCheatFightWin(token);
        }
        else {
            this.readCheatFightLoose(token);
        }
    }

    private void readCreateBrute(String token, String name) throws IOException, SQLException, NetworkResponseException, NotFoundEntityException {
        User user = this.checkTokenAndReturnUser(token);
        
        // Brute already exists !
        /*if( BruteEntity.findByUser(user) != null ) {
            throw new NetworkResponseException(Protocol.ERROR_CREATE_BRUTE);
        }*/
        
        if( name.isEmpty() ) {
            throw new NetworkResponseException(Protocol.ERROR_CREATE_BRUTE); // @TODO Protocol.ERROR_INPUT_DATAS
        }
        
        /* @TODO
         * if( BruteEntity.findByName(name) != null ) {
         *    throw new NetworkResponseException(Protocol.ERROR_BRUTE_ALREADY_USED);
         * }
         */
        
        short level = 1;
        short strength = (short) ui.random(3, 10);
        short speed    = (short) ui.random(3, 10);
        short life     = (short) (ui.random(10, 20) + strength/3);
        int imageID = ui.random(1, 13);
        
        Brute brute = new Brute(0, name, level, life, strength, speed, imageID);
        brute.setUserId(user.getId());
        //brute.setImageID(imageID);
        DatasManager.insert(brute);
        
        this.getWriter().writeDiscriminant(Protocol.R_ACTION_SUCCESS)
                .send();
    }

    private void readUpdateBrute(String token, String name) throws IOException, SQLException, NetworkResponseException, NotFoundEntityException {
        User user = this.checkTokenAndReturnUser(token);
        
        if( name.isEmpty() ) {
            throw new NetworkResponseException(Protocol.ERROR_UPDATE_BRUTE); // @TODO Protocol.ERROR_INPUT_DATAS
        }
        
        /* @TODO define it !
         * if( BruteEntity.findByName(name) != null ) {
         *    throw new NetworkResponseException(Protocol.ERROR_BRUTE_ALREADY_USED);
         * }
         */
        
        Brute brute = BruteEntity.findOneByUser(user);
        brute.setName(name);
        DatasManager.save(brute);
        
        this.getWriter().writeDiscriminant(Protocol.R_ACTION_SUCCESS)
                .send();
    }

    private void readDeleteBrute(String token) throws IOException, SQLException, NetworkResponseException, NotFoundEntityException {
        User user = this.checkTokenAndReturnUser(token);
        Brute brute = BruteEntity.findOneByUser(user);
        
        DatasManager.delete(brute);
        
        this.getWriter().writeDiscriminant(Protocol.R_ACTION_SUCCESS)
                .send();
    }

    private void readDataBonus(int id) throws IOException, SQLException, NetworkResponseException, NotFoundEntityException {
        Bonus bonus = BonusEntity.findOneById(id);
        System.out.println("readDataBonus(" + id + ") : imageID=" + bonus.getImageID());
        this.getWriter().writeDiscriminant(Protocol.R_DATA_BONUS)
                .writeLongInt(id)
                .writeString(bonus.getName())
                .writeShortInt((short) bonus.getLevel())
                .writeShortInt((short) bonus.getStrength())
                .writeShortInt((short) bonus.getSpeed())
                .writeLongInt(bonus.getImageID())
                .send();
    }

    private void readDataBrute(int id) throws IOException, SQLException, NetworkResponseException, NotFoundEntityException {
        Brute brute = BruteEntity.findOneById(id);

        brute.setBonuses(BonusEntity.findAllByBrute(brute));

        this.getWriter().writeDiscriminant(Protocol.R_DATA_BRUTE)
                .writeLongInt(id)
                .writeString(brute.getName())
                .writeShortInt((short) brute.getLevel())
                .writeShortInt((short) brute.getLife())
                .writeShortInt((short) brute.getStrength())
                .writeShortInt((short) brute.getSpeed())
                .writeLongInt(brute.getImageID())
                .writeLongIntArray(brute.getBonusesIDs())
                .send();
    }

    private void readGetChallengerBruteId(String token) throws IOException, SQLException, NetworkResponseException, NotFoundEntityException {
        User user = this.checkTokenAndReturnUser(token);
        Fight fight = this.getFightWithChallenger(user);

        this.getWriter().writeDiscriminant(Protocol.R_BRUTE)
                .writeLongInt(fight.getBrute2().getId())
                .send();
    }

    private void readGetMyBruteId(String token) throws IOException, SQLException, NetworkResponseException, NotFoundEntityException {
        User user = this.checkTokenAndReturnUser(token);
        Brute brute = BruteEntity.findOneByUser(user);
        
        this.getWriter().writeDiscriminant(Protocol.R_BRUTE)
                .writeLongInt(brute.getId())
                .send();
    }
    
    private void readCreateUser(String pseudo, String password) throws IOException, SQLException, NetworkResponseException {
        if( pseudo.isEmpty() || password.isEmpty() ) {
            throw new NetworkResponseException(Protocol.ERROR); // @TODO Protocol.ERROR_INPUT_DATAS
        }
        
        
        /* @TODO define it !
         * if( UserEntity.findByPseudo(pseudo) != null ) {
         *    throw new NetworkResponseException(Protocol.ERROR_USER_ALREADY_USED);
         * }
         */
        
        User user = new User(0, pseudo, password, null);
        DatasManager.insert(user);
        
        this.getWriter().writeDiscriminant(Protocol.R_ACTION_SUCCESS)
                .send();
        
    }
    
    private void readDeleteUser(String token) throws IOException, SQLException, NetworkResponseException, NotFoundEntityException {
        User user = this.checkTokenAndReturnUser(token);
        DatasManager.delete(user);
    
        this.getWriter().writeDiscriminant(Protocol.R_ACTION_SUCCESS)
                .send();
    }

    private Fight getFightWithChallenger(User user) throws IOException, SQLException, NotFoundEntityException, NetworkResponseException {
        Brute brute = BruteEntity.findOneByUser(user);
        
        Fight fight = FightEntity.findByUser(user); // and not findOneByUser
        
        if (fight == null) {
            Brute otherBrute = BruteEntity.findOneRandomAnotherToBattleByUser(user);
            
            fight = new Fight();
            fight.setBrute1(brute);
            fight.setBrute2(otherBrute);
            DatasManager.insert(fight);
        }
        
        if( fight == null ) {
            throw new NetworkResponseException(Protocol.ERROR_FIGHT);
        }
        return fight;
    }

    private void readGetImage(int id) throws NetworkResponseException {
        String fileUrl = "res/" + id + ".png";
        
        if( id == 0 ) {
            System.out.println("SRLY_WTF " + fileUrl);
            throw new NetworkResponseException(Protocol.ERROR_SRLY_WTF);
        }
        
        if( !(new File(fileUrl)).exists() ) {
            System.out.println("NOT_FOUND " + fileUrl);
            throw new NetworkResponseException(Protocol.ERROR_IMAGE_NOT_FOUND);
        }
        System.out.println("IMG:" + fileUrl);
        
        this.getWriter().writeDiscriminant(Protocol.R_DATA_IMAGE)
                .writeLongInt(id)
                .writeImage(fileUrl)
                .send();
    }
}
