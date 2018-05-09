package ftpstorage.client.data;

public interface PieceListener {

    public void transferPieceComplete(Piece p);

    public void notifyInteruptedPiece(Piece p);
    
}
